package ru.fozeton.chatmanager.network;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.NetworkConfig;
import ru.fozeton.chatmanager.messages.Message;
import ru.fozeton.chatmanager.utils.Logger;
import ru.fozeton.chatmanager.utils.compat.providers.ComponentSerializerProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class WebHooksHandler {
    private final HttpClient client = NetworkManager.getInstance().getClient();
    private final NetworkConfig networkConfig = ChatConfigManager.getInstance().getNetworkConfig();
    private final Logger log = new Logger(WebHooksHandler.class);
    private final Gson gson = new Gson();

    protected void onWebHook(Message message) {
        Optional<ChatChannel> messageChannel = message.getChannel();
        NetworkConfig.WebHook localWebHook = null;
        NetworkConfig.WebHook globalWebHook = networkConfig.getGlobalWebHook();
        if (messageChannel.isPresent()) {
            ChatChannel channel = messageChannel.get();
            if (networkConfig.getIgnoreChannels().contains(channel.getId())) return;
            localWebHook = networkConfig.getChannelsWebHooks().get(channel.getId());
        }

        String targetUrl = resolveWebhookUrl(localWebHook, globalWebHook);
        if (targetUrl == null) return;

        try {
            String payload = resolveWebhookCleanText(
                    localWebHook,
                    globalWebHook
            ) ? message.getFullPlain() : ComponentSerializerProvider.toJson(message.getContent());
            NetworkMessage networkMessage = new NetworkMessage(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(networkMessage)))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 400) {
                            log.error("Failed to send webhook. Server returned status code: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error occurred while sending webhook: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Failed to build or send HTTP request" + e);
        }
    }

    private boolean isUsable(@Nullable NetworkConfig.WebHook webHook) {
        return webHook != null && webHook.isEnable() && !webHook.getUrl().isBlank();
    }

    @Nullable
    private String resolveWebhookUrl(@Nullable NetworkConfig.WebHook local, NetworkConfig.WebHook global) {
        if (isUsable(local)) return local.getUrl();
        if (isUsable(global)) return global.getUrl();
        return null;
    }

    private boolean resolveWebhookCleanText(@Nullable NetworkConfig.WebHook local, NetworkConfig.WebHook global) {
        if (isUsable(local)) return local.isCleanText();
        if (isUsable(global)) return global.isCleanText();
        return false;
    }

    private record NetworkMessage(String content) {
    }
}
