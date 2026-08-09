package ru.fozeton.chatmanager.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.speech.*;
import ru.fozeton.chatmanager.utils.Logger;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SpeechToText implements AutoCloseable {
    private final Logger log = new Logger(SpeechToText.class);
    private final HttpClient client = HttpClient.newHttpClient();
    private final Path models = ChatManagerCore.CONFIG_DIR.resolve("language_models");
    private final VoskModel model;
    private final Language language;

    private SpeechToText(Builder builder) {
        this.model = builder.model;
        this.language = builder.language;
    }

    private static boolean hasModelFiles(Path path) throws IOException {
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

    public void transcription() throws IOException, RuntimeException {
        Path languagePath = models.resolve(language.getPath());

        if (!Files.exists(languagePath)) Files.createDirectories(languagePath);

        if (hasModelFiles(languagePath)) {
            ChatManagerCore.EVENT_BUS.activate(new VoskModelAbsentEvent());
            throw new IllegalStateException("Vosk model files not found: " + languagePath);
        }

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            ChatManagerCore.EVENT_BUS.activate(new VoskNotSupportMicroEvent());
            return;
        }

        try (
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                InputStream ais = new AudioInputStream(line);
                VoskRecognizer recognizer = VoskContext.getFactory().createRecognizer(model, 16000)
        ) {
            line.open(format);
            line.start();

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
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
        }
    }

    public void download(Language languageModel) {
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

    @Override
    public void close() {
        model.close();
    }

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

        private final String path;
        private final String model;
        private final String downloadPath;
        private final int sizeMb;

        Language(String path, String model, int sizeMb) {
            this.path = path;
            this.model = model;
            this.downloadPath = "https://alphacephei.com/vosk/models/" + model + ".zip";
            this.sizeMb = sizeMb;
        }
    }

    public static class Builder {
        private final VoskModel model;
        private final Language language;

        public Builder(Language language) throws IOException {
            Path models = ChatManagerCore.CONFIG_DIR.resolve("language_models");
            Path languagePath = models.resolve(language.getPath());
            Path modelPath = languagePath.resolve(language.getModel());

            if (hasModelFiles(languagePath)) {
                ChatManagerCore.EVENT_BUS.activate(new VoskModelAbsentEvent());
                throw new IllegalStateException("Vosk model files not found: " + modelPath);
            }

            try {
                this.model = VoskContext.getFactory().createModel(modelPath.toString());
                this.language = language;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public SpeechToText build() {
            return new SpeechToText(this);
        }
    }
}