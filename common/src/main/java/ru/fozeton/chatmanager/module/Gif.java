package ru.fozeton.chatmanager.module;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.exceptions.GifException;
import ru.fozeton.chatmanager.module.gif.GifsResponse;
import ru.fozeton.chatmanager.module.gif.McAnim;
import ru.fozeton.chatmanager.network.NetworkManager;
import ru.fozeton.chatmanager.utils.compat.providers.GameProfileProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Gif {
    public static final int PER_PAGE = 24;

    @Getter
    private static final Gif instance = new Gif();
    private static final String BASE_URL = "https://core.chatmanager.workers.dev/gifs/";
    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final Path gifPath = ChatManagerCore.getConfigDir().resolve("cache");
    private final HttpClient client = NetworkManager.getInstance().getClient();
    private final Gson gson = new Gson();

    private static boolean isNotWebp(byte[] bytes) {
        return bytes == null || bytes.length <= 12
               || bytes[0] != 'R' || bytes[1] != 'I' || bytes[2] != 'F' || bytes[3] != 'F'
               || bytes[8] != 'W' || bytes[9] != 'E' || bytes[10] != 'B' || bytes[11] != 'P';
    }

    private String customerParam() {
        try {
            String id = String.format("%s_%s", GameProfileProvider.getName(), GameProfileProvider.getId());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(id.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return "&customer_id=" + URLEncoder.encode(hexString.toString(), UTF_8);
        } catch (NoSuchAlgorithmException ex) {
            throw new GifException(ex);
        }
    }

    public CompletableFuture<Boolean> download(String gifId) {
        return download(gifId, gifPath.toString());
    }

    public CompletableFuture<Boolean> download(String gifId, String path) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Files.createDirectories(Path.of(path));
                        byte[] webpBytes = getWebpBytes(gifId).get();
                        if (isNotWebp(webpBytes)) return false;
                        return McAnim.INSTANCE.convert_webp(
                                webpBytes,
                                webpBytes.length,
                                gifId + ".mcanim",
                                path
                        );
                    } catch (InterruptedException | ExecutionException | IOException e) {
                        throw new GifException(e);
                    }
                }, EXECUTOR
        );
    }

    public CompletableFuture<Boolean> downloadFromUrl(String url, String name, String dir) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        byte[] webp = client.send(
                                HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                                HttpResponse.BodyHandlers.ofByteArray()
                        ).body();
                        if (isNotWebp(webp)) return false;
                        return McAnim.INSTANCE.convert_webp(webp, webp.length, name + ".mcanim", dir);
                    } catch (IOException | InterruptedException e) {
                        throw new GifException(e);
                    }
                }, EXECUTOR
        );
    }

    private CompletableFuture<byte[]> getWebpBytes(String gifId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(
                                    BASE_URL + "items?slugs=" + URLEncoder.encode(gifId, UTF_8) + customerParam()))
                            .GET()
                            .build();

                    try {
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() != 200) {
                            throw new GifException("items HTTP " + response.statusCode() + " for " + gifId);
                        }

                        GifsResponse list = gson.fromJson(response.body(), GifsResponse.class);
                        if (list == null || list.getData() == null
                            || list.getData().getData() == null || list.getData().getData().isEmpty()) {
                            throw new GifException("Gif not found: " + gifId);
                        }

                        String url = list.getData().getData().getFirst().getFile().getXs().getAnimated().getUrl();
                        HttpRequest gifRequest = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .GET()
                                .build();

                        return client.send(gifRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
                    } catch (IOException e) {
                        throw new GifException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new GifException(e);
                    }
                }, EXECUTOR
        );
    }

    public boolean existsGif(String gifName) {
        return Files.exists(gifPath.resolve(gifName));
    }

    private GifsResponse fetchList(String endpoint, String query, int page) {
        StringBuilder url = new StringBuilder(BASE_URL)
                .append(endpoint)
                .append("?format_filter=webp&page=").append(page)
                .append("&per_page=").append(PER_PAGE)
                .append(customerParam());
        if (query != null) url.append("&q=").append(URLEncoder.encode(query, UTF_8));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url.toString())).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), GifsResponse.class);
        } catch (IOException e) {
            throw new GifException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GifException(e);
        }
    }

    public GifsResponse search(String query) {
        return search(query, 1);
    }

    public GifsResponse search(String query, int page) {
        return fetchList("search", query, page);
    }

    public GifsResponse trending() {
        return trending(1);
    }

    public GifsResponse trending(int page) {
        return fetchList("trending", null, page);
    }
}