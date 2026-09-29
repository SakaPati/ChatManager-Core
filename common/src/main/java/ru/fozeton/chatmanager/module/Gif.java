package ru.fozeton.chatmanager.module;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.module.gif.GifResponse;
import ru.fozeton.chatmanager.module.gif.GifsResponse;
import ru.fozeton.chatmanager.module.gif.McAnim;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Gif {
    @Getter
    private static final Gif instance = new Gif();
    private static final String BASE_URL = "https://api.klipy.com/api/v1/******/gifs/";
    private final Path gifPath = ChatManagerCore.getConfigDir().resolve("cache");
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public CompletableFuture<Boolean> download(String gifId) {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + gifId))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                GifResponse gifResponse = gson.fromJson(response.body(), GifResponse.class);
                HttpRequest gifRequest = HttpRequest.newBuilder()
                        .uri(URI.create(gifResponse.getData().getFile().getXs().getAnimated().getUrl()))
                        .GET()
                        .build();

                byte[] webpBytes = client.send(gifRequest, HttpResponse.BodyHandlers.ofByteArray()).body();

                return McAnim.INSTANCE.convert_webp(
                        webpBytes,
                        webpBytes.length,
                        gifId + ".mcanim",
                        gifPath.toString()
                );
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    public boolean existsGif(String gifName) {
        return Files.exists(gifPath.resolve(gifName));
    }

    public GifsResponse search(String query) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<GifsResponse> future = executor.submit(() -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "search?format_filter=webp&q=" + query))
                        .GET()
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return gson.fromJson(response.body(), GifsResponse.class);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public GifsResponse trending() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<GifsResponse> future = executor.submit(() -> {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "trending?format_filter=webp"))
                        .GET()
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return gson.fromJson(response.body(), GifsResponse.class);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
