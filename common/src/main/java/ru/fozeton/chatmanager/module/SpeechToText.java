package ru.fozeton.chatmanager.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.VoiceConfig;
import ru.fozeton.chatmanager.events.speech.*;
import ru.fozeton.chatmanager.network.NetworkManager;import ru.fozeton.chatmanager.utils.Logger;
import ru.fozeton.chatmanager.utils.stt.VoiceIndicator;
import ru.fozeton.chatmanager.utils.stt.VoskContext;
import ru.fozeton.chatmanager.utils.stt.VoskModel;
import ru.fozeton.chatmanager.utils.stt.VoskRecognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles offline Speech-to-Text (STT) transcription using the Vosk library.
 * <p>
 * This class captures audio from the system's default microphone and passes it
 * to the Vosk recognizer to convert speech into text. It also handles the
 * downloading and extraction of language models asynchronously.
 * <p>
 * Loading a model is expensive (up to several GB of native memory and a long load time),
 * so the loaded model is cached statically. Always obtain instances through
 * {@link #get(Language)} instead of creating a {@link Builder} manually, and release
 * the memory with {@link #unload()} when the model is no longer needed
 * (game exit, model switch, model deletion).
 * <p>
 * Usage:
 * <pre>
 * SpeechToText.get(SpeechToText.Language.RUSSIAN_SMALL).transcription(); // blocks until recognition ends
 * </pre>
 * <p>
 * Model state:
 * <ul>
 *     <li>{@link #isInstalled(Language)} - check whether a model exists on disk;</li>
 *     <li>{@link #download(Language)} - download a model (async, reports via events);</li>
 *     <li>{@link #get(Language)} - load a model into memory (cached);</li>
 *     <li>{@link #unload()} - free the cached model.</li>
 * </ul>
 * <p>
 * Instances are not meant to run {@link #transcription()} concurrently: the native
 * model must not be unloaded while a recognition is in progress.
 */
public class SpeechToText {
    private static final Logger log = new Logger(SpeechToText.class);
    private static final HttpClient client = NetworkManager.getInstance().getClient();

    /**
     * Root directory where all language models are stored: {@code <config>/language_models}.
     */
    private static final Path models = ChatManagerCore.getConfigDir().resolve("language_models");

    /**
     * Currently loaded instance, or {@code null} if nothing is loaded. Guarded by the class monitor.
     */
    private static SpeechToText cached;

    /**
     * Language of {@link #cached}. Guarded by the class monitor.
     */
    private static Language cachedLanguage;

    private final VoiceConfig config = ChatConfigManager.getInstance().getVoiceConfig();

    /**
     * Native Vosk model owned by this instance. Closed only through {@link #unload()}.
     */
    private final VoskModel model;
    private final Language language;

    /**
     * Private constructor used by the {@link Builder}.
     *
     * @param builder the builder instance containing the configured model and language
     */
    private SpeechToText(Builder builder) {
        this.model = builder.model;
        this.language = builder.language;
    }

    /**
     * Checks that the specified language directory does NOT contain an extracted Vosk model folder.
     * Note the inverted meaning: {@code true} means the model is missing.
     *
     * @param path the path to the language directory (e.g. {@code language_models/ru})
     * @return true if no {@code vosk-model*} subdirectory exists, false otherwise
     * @throws IOException if an I/O error occurs when opening the directory
     */
    private static boolean isModelMissing(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "vosk-model*")) {
            boolean hasDir = false;

            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    hasDir = true;
                    break;
                }
            }

            return !hasDir;
        }
    }

    /**
     * Returns a ready-to-use instance for the given language, loading the model if needed.
     * <p>
     * If the requested language is already loaded, the cached instance is returned immediately.
     * If a different language is cached, it is unloaded first and the new one is loaded, so at most
     * one model is kept in memory at a time. The first call for a language is slow, since the model
     * is read from disk.
     * <p>
     * If loading fails, nothing is cached and the next call retries from scratch.
     *
     * @param language the language model to load
     * @return the cached or newly created instance
     * @throws RuntimeException if the model is missing or fails to load natively
     *                          (in that case a {@link VoskModelAbsentEvent} is fired by the {@link Builder})
     */
    public static synchronized SpeechToText get(Language language) {
        if (cached != null && cachedLanguage == language) return cached;

        unload();
        cached = new Builder(language).build();
        cachedLanguage = language;
        return cached;
    }

    /**
     * Unloads the cached model and releases its native memory. Does nothing if no model is loaded.
     * <p>
     * Must not be called while {@link #transcription()} is running on the cached instance,
     * as the native code may crash the JVM. Call it on game shutdown, before switching or
     * deleting a model on disk (open files may be locked on Windows).
     */
    public static synchronized void unload() {
        if (cached != null) {
            cached.model.close();
            cached = null;
            cachedLanguage = null;
        }
    }

    /** True while transcription() is running; the cached model must not be unloaded or deleted then. */
    private static volatile boolean busy;

    /**
     * Deletes the model folder from disk. If the model is currently loaded in memory, it is unloaded first.
     *
     * @param language the model to delete
     * @return true if the model is gone, false if it is in use (recording) or deletion failed
     */
    public static synchronized boolean delete(Language language) {
        if (busy && cachedLanguage == language) return false;
        if (cachedLanguage == language) unload();

        Path dir = models.resolve(language.getPath()).resolve(language.getModel());
        if (!Files.exists(dir)) return true;

        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
            return true;
        } catch (IOException e) {
            log.error("Error deleting model " + language + ": " + e);
            return false;
        }
    }

    /**
     * Downloads and extracts the specified Vosk language model asynchronously in a virtual thread.
     * Returns immediately. Fires {@link VoskModelDownloadSuccessEvent} on success or
     * {@link VoskModelDownloadFailedEvent} on failure; these events are fired from the download
     * thread, not from the main thread.
     * <p>
     * The archive is streamed and extracted on the fly into {@code language_models/<path>/}.
     * Calling this method twice for the same model starts two parallel downloads, so the caller
     * should track which models are already downloading.
     * <p>
     * Note: extraction is not atomic. If the connection drops mid-way, a partially extracted
     * model folder may remain on disk and {@link #isInstalled(Language)} will report it as installed.
     *
     * @param languageModel the language model enum containing the download URL and paths
     */
    public static void download(Language languageModel) {
        Thread.ofVirtual()
                .name("CM-Download-" + languageModel.name())
                .start(() -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(languageModel.downloadPath))
                                .build();

                        HttpResponse<InputStream> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofInputStream()
                        );

                        Path path = models.resolve(languageModel.path);
                        if (!Files.exists(path)) Files.createDirectories(path);

                        try (ZipInputStream zis = new ZipInputStream(response.body())) {
                            ZipEntry entry;

                            while ((entry = zis.getNextEntry()) != null) {
                                if (!entry.isDirectory()) {
                                    Path targetFile = path.resolve(entry.getName());
                                    if (targetFile.getParent() != null) Files.createDirectories(targetFile.getParent());

                                    Files.copy(zis, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                }
                                zis.closeEntry();
                            }
                        }

                        ChatManagerCore.EVENT_BUS.activate(new VoskModelDownloadSuccessEvent(languageModel));
                    } catch (Exception e) {
                        log.error("Error downloading model " + languageModel + ": " + e);
                        ChatManagerCore.EVENT_BUS.activate(new VoskModelDownloadFailedEvent(languageModel, e));
                    }
                });
    }

    /**
     * Checks whether the model folder of the given language exists on disk.
     * This only checks for the directory, not the integrity of its contents.
     *
     * @param language the language model to check
     * @return true if {@code language_models/<path>/<model>} is an existing directory
     */
    public static boolean isInstalled(Language language) {
        return Files.isDirectory(models.resolve(language.getPath()).resolve(language.getModel()));
    }

    /**
     * Starts the audio capture and transcription process. This call BLOCKS the current thread,
     * so run it off the main thread (e.g. in a virtual thread).
     * <p>
     * Captures audio from the default microphone (16 kHz, 16-bit, mono) and feeds it to a new
     * Vosk recognizer. While the user speaks, {@link VoskPartialResultEvent} is fired with the
     * intermediate text. Recognition finishes when Vosk reports a final result AND no voice
     * (level &gt;= 0.15) has been detected for {@link VoiceConfig#getVoiceDelayMs()} milliseconds;
     * then {@link VoskResultEvent} is fired with the final text and the method returns.
     * <p>
     * If the microphone line is not supported, {@link VoskNotSupportMicroEvent} is fired and the
     * method returns without throwing. {@link VoiceIndicator#finished()} is always called on exit.
     *
     * @throws IOException      if an I/O error occurs while preparing the language directory
     * @throws RuntimeException if audio line initialization fails or Vosk encounters an error
     */
    public void transcription() throws IOException, RuntimeException {
        Path languagePath = models.resolve(language.getPath());
        long lastActiveVoice = 0;

        if (!Files.exists(languagePath)) Files.createDirectories(languagePath);

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            ChatManagerCore.EVENT_BUS.activate(new VoskNotSupportMicroEvent());
            VoiceIndicator.error();
            return;
        }

        busy = true;
        try (
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                InputStream ais = new AudioInputStream(line);
                VoskRecognizer recognizer = VoskContext.getFactory().createRecognizer(model, 16000)
        ) {
            line.open(format);
            line.start();
            VoiceIndicator.listening();

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                float level = VoiceIndicator.audio(b, nbytes);
                long millis = System.currentTimeMillis();
                if (level >= 0.15) lastActiveVoice = millis;
                if (recognizer.acceptWaveForm(b, nbytes) && (millis > lastActiveVoice + config.getVoiceDelayMs())) {
                    String rawResult = recognizer.getResult();

                    JsonObject jsonObject = JsonParser.parseString(rawResult).getAsJsonObject();
                    String result = jsonObject.get("text").getAsString();

                    ChatManagerCore.EVENT_BUS.activate(new VoskResultEvent(result));
                    return;
                } else {
                    String rawPartialResult = recognizer.getPartialResult();
                    JsonObject jsonObject = JsonParser.parseString(rawPartialResult).getAsJsonObject();
                    String partialResult = jsonObject.get("partial").getAsString();

                    ChatManagerCore.EVENT_BUS.activate(new VoskPartialResultEvent(partialResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            busy = false;
            VoiceIndicator.finished();
        }
    }

    /**
     * Enum representing available Vosk STT language models.
     * Contains the language folder, the exact model name, the download URL and the approximate size in MB.
     * <p>
     * Models are stored as {@code language_models/<path>/<model>/} and downloaded from
     * {@code https://alphacephei.com/vosk/models/<model>.zip}. Several models can share the same
     * {@code path} (e.g. small and full versions of one language).
     * The enum constant name (e.g. {@code RUSSIAN_SMALL}) is what {@link VoiceConfig#getModel()} stores.
     */
    @Getter
    public enum Language {
        ENGLISH_US_SMALL("en-us", "vosk-model-small-en-us-0.15", 40),
        ENGLISH_US("en-us", "vosk-model-en-us-0.22", 1843),
        ENGLISH_US_LGRAPH("en-us", "vosk-model-en-us-0.22-lgraph", 128),
        ENGLISH_US_GIGASPEECH("en-us", "vosk-model-en-us-0.42-gigaspeech", 2355),
        ENGLISH_US_DAANZU("en-us", "vosk-model-en-us-daanzu-20200905", 1024),
        ENGLISH_US_DAANZU_LGRAPH("en-us", "vosk-model-en-us-daanzu-20200905-lgraph", 129),
        ENGLISH_US_LIBRISPEECH("en-us", "vosk-model-en-us-librispeech-0.2", 845),
        ENGLISH_US_ZAMIA_SMALL("en-us", "vosk-model-small-en-us-zamia-0.5", 49),
        ENGLISH_US_ASPIRE("en-us", "vosk-model-en-us-aspire-0.2", 1434),
        ENGLISH_US_OLD("en-us", "vosk-model-en-us-0.21", 1638),
        ENGLISH_INDIAN("en-in", "vosk-model-en-in-0.5", 1024),
        ENGLISH_INDIAN_SMALL("en-in", "vosk-model-small-en-in-0.4", 36),
        CHINESE_SMALL("cn", "vosk-model-small-cn-0.22", 42),
        CHINESE("cn", "vosk-model-cn-0.22", 1331),
        CHINESE_MULTICN("cn", "vosk-model-cn-kaldi-multicn-0.15", 1536),
        RUSSIAN("ru", "vosk-model-ru-0.42", 1843),
        RUSSIAN_SMALL("ru", "vosk-model-small-ru-0.22", 45),
        RUSSIAN_022("ru", "vosk-model-ru-0.22", 1536),
        RUSSIAN_010("ru", "vosk-model-ru-0.10", 2560),
        FRENCH_SMALL("fr", "vosk-model-small-fr-0.22", 41),
        FRENCH("fr", "vosk-model-fr-0.22", 1434),
        FRENCH_PGUYOT_SMALL("fr", "vosk-model-small-fr-pguyot-0.3", 39),
        FRENCH_LINTO("fr", "vosk-model-fr-0.6-linto-2.2.0", 1536),
        GERMAN("de", "vosk-model-de-0.21", 1946),
        GERMAN_TUDA("de", "vosk-model-de-tuda-0.6-900k", 4506),
        GERMAN_ZAMIA_SMALL("de", "vosk-model-small-de-zamia-0.3", 49),
        GERMAN_SMALL("de", "vosk-model-small-de-0.15", 45),
        SPANISH_SMALL("es", "vosk-model-small-es-0.42", 39),
        SPANISH("es", "vosk-model-es-0.42", 1434),
        PORTUGUESE_SMALL("pt", "vosk-model-small-pt-0.3", 31),
        PORTUGUESE("pt", "vosk-model-pt-fb-v0.1.1-20220516_2113", 1638),
        GREEK("el", "vosk-model-el-gr-0.7", 1126),
        TURKISH_SMALL("tr", "vosk-model-small-tr-0.3", 35),
        VIETNAMESE_SMALL("vn", "vosk-model-small-vn-0.4", 32),
        VIETNAMESE("vn", "vosk-model-vn-0.4", 78),
        ITALIAN_SMALL("it", "vosk-model-small-it-0.22", 48),
        ITALIAN("it", "vosk-model-it-0.22", 1229),
        DUTCH_SMALL("nl", "vosk-model-small-nl-0.22", 39),
        DUTCH_SPRAAKHERKENNING("nl", "vosk-model-nl-spraakherkenning-0.6", 860),
        DUTCH_SPRAAKHERKENNING_LGRAPH("nl", "vosk-model-nl-spraakherkenning-0.6-lgraph", 100),
        CATALAN_SMALL("ca", "vosk-model-small-ca-0.4", 42),
        ARABIC_MGB2("ar", "vosk-model-ar-mgb2-0.4", 318),
        ARABIC_LINTO("ar", "vosk-model-ar-0.22-linto-1.1.0", 1331),
        ARABIC_TUNISIAN_SMALL("ar-tn", "vosk-model-small-ar-tn-0.1-linto", 158),
        ARABIC_TUNISIAN("ar-tn", "vosk-model-ar-tn-0.1-linto", 517),
        FARSI("fa", "vosk-model-fa-0.42", 1638),
        FARSI_SMALL("fa", "vosk-model-small-fa-0.42", 53),
        FARSI_OLD("fa", "vosk-model-fa-0.5", 1024),
        FARSI_SMALL_OLD("fa", "vosk-model-small-fa-0.5", 60),
        FILIPINO("tl", "vosk-model-tl-ph-generic-0.6", 320),
        UKRAINIAN_NANO("uk", "vosk-model-small-uk-v3-nano", 73),
        UKRAINIAN_SMALL("uk", "vosk-model-small-uk-v3-small", 133),
        UKRAINIAN("uk", "vosk-model-uk-v3", 343),
        UKRAINIAN_LGRAPH("uk", "vosk-model-uk-v3-lgraph", 325),
        KAZAKH_SMALL("kz", "vosk-model-small-kz-0.42", 58),
        KAZAKH("kz", "vosk-model-kz-0.42", 1331),
        SWEDISH_SMALL("sv", "vosk-model-small-sv-rhasspy-0.15", 289),
        JAPANESE_SMALL("ja", "vosk-model-small-ja-0.22", 48),
        JAPANESE("ja", "vosk-model-ja-0.22", 1024),
        ESPERANTO_SMALL("eo", "vosk-model-small-eo-0.42", 42),
        HINDI_SMALL("hi", "vosk-model-small-hi-0.22", 42),
        HINDI("hi", "vosk-model-hi-0.22", 1536),
        CZECH_SMALL("cs", "vosk-model-small-cs-0.4-rhasspy", 44),
        POLISH_SMALL("pl", "vosk-model-small-pl-0.22", 50),
        UZBEK_SMALL("uz", "vosk-model-small-uz-0.22", 49),
        KOREAN_SMALL("ko", "vosk-model-small-ko-0.22", 82),
        BRETON("br", "vosk-model-br-0.8", 70),
        GUJARATI("gu", "vosk-model-gu-0.42", 700),
        GUJARATI_SMALL("gu", "vosk-model-small-gu-0.42", 100),
        TAJIK("tg", "vosk-model-tg-0.22", 327),
        TAJIK_SMALL("tg", "vosk-model-small-tg-0.22", 50),
        TELUGU_SMALL("te", "vosk-model-small-te-0.42", 58),
        KYRGYZ_SMALL("ky", "vosk-model-small-ky-0.42", 49),
        KYRGYZ("ky", "vosk-model-ky-0.42", 1126),
        GEORGIAN_SMALL("ka", "vosk-model-small-ka-0.42", 45),
        GEORGIAN("ka", "vosk-model-ka-0.42", 700),
        SPEAKER_ID("spk", "vosk-model-spk-0.4", 13);

        /**
         * Language folder under {@code language_models}, e.g. {@code ru}, {@code en-us}.
         */
        private final String path;

        /**
         * Exact model name, also the name of the extracted folder, e.g. {@code vosk-model-small-ru-0.22}.
         */
        private final String model;

        /**
         * Full URL of the model archive.
         */
        private final String downloadPath;

        /**
         * Approximate model size in megabytes (archive/extracted, for display purposes).
         */
        private final int sizeMb;

        Language(String path, String model, int sizeMb) {
            this.path = path;
            this.model = model;
            this.downloadPath = "https://alphacephei.com/vosk/models/" + model + ".zip";
            this.sizeMb = sizeMb;
        }
    }

    /**
     * Builder class for creating configured {@link SpeechToText} instances.
     * Loads the native Vosk model from disk. Prefer {@link SpeechToText#get(Language)},
     * which wraps this builder with caching; creating instances manually bypasses the cache
     * and leaves the model's native memory unmanaged.
     */
    public static class Builder {
        private final VoskModel model;
        private final Language language;

        /**
         * Loads the requested language model from {@code language_models/<path>/<model>}.
         * This is a slow, memory-heavy operation for large models.
         *
         * @param language the target STT language
         * @throws RuntimeException if the model is missing or fails to instantiate natively;
         *                          a {@link VoskModelAbsentEvent} with the expected path is fired first
         */
        public Builder(Language language) {
            Path models = ChatManagerCore.getConfigDir().resolve("language_models");
            Path languagePath = models.resolve(language.getPath());
            Path modelPath = languagePath.resolve(language.getModel());

            try {
                this.model = VoskContext.getFactory().createModel(modelPath.toString());
                this.language = language;
            } catch (Exception e) {
                ChatManagerCore.EVENT_BUS.activate(new VoskModelAbsentEvent(modelPath));
                throw new RuntimeException(e);
            }
        }

        public SpeechToText build() {
            return new SpeechToText(this);
        }
    }
}