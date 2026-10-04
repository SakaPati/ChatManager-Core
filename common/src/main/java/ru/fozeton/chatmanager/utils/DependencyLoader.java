package ru.fozeton.chatmanager.utils;

import ru.fozeton.chatmanager.network.NetworkManager;import ru.fozeton.chatmanager.utils.stt.VoskContext;
import ru.fozeton.chatmanager.utils.stt.VoskFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class responsible for dynamically downloading and loading external dependencies
 * at runtime, specifically the Vosk library.
 * <p>
 * This allows the plugin/application to stay lightweight and not bundle heavy native
 * wrapper JARs by default.
 */
public class DependencyLoader {
    private static final HttpClient client = NetworkManager.getInstance().getClient();
    private static final String VOSK_URL = "https://repo1.maven.org/maven2/com/alphacephei/vosk/0.3.45/vosk-0.3.45.jar";

    /**
     * Ensures that the required Vosk library is present in the specified directory,
     * downloads it if missing, and injects it into the application's classpath dynamically.
     *
     * @param libsFolder the folder where dependencies should be stored
     * @throws RuntimeException if the download or classpath injection fails
     */
    public static void loadDependencies(Path libsFolder) {
        try {
            if (!Files.exists(libsFolder)) Files.createDirectories(libsFolder);
            Path voskJar = libsFolder.resolve("vosk-0.3.45.jar");

            downloadIfAbsent(voskJar);

            System.setProperty("jna.nosys", "true");
            System.setProperty("jna.encoding", "UTF-8");

            addToClasspath(voskJar);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load dependency Vosk", e);
        }
    }

    /**
     * Checks if the target JAR exists, and if not, downloads it from the Maven central repository.
     *
     * @param target the path where the JAR should be saved
     * @throws Exception if an I/O or network error occurs during downloading
     */
    private static void downloadIfAbsent(Path target) throws Exception {
        if (!Files.exists(target)) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DependencyLoader.VOSK_URL)).build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream is = response.body()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Creates a custom URLClassLoader for the downloaded JAR and passes it to the
     * global {@link VoskContext} so that proxy classes can be generated.
     *
     * @param jarPath the local filesystem path to the downloaded JAR file
     * @throws RuntimeException if the path cannot be converted to a valid URL
     */
    public static void addToClasspath(Path jarPath) {
        try {
            URL url = jarPath.toUri().toURL();
            URLClassLoader cl = new URLClassLoader(new URL[]{url}, DependencyLoader.class.getClassLoader());
            VoskContext.setFactory(new VoskFactory(cl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}