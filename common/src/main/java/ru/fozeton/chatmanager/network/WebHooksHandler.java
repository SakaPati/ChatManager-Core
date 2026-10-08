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

/**
 * Sends chat messages to HTTP webhooks configured in {@link NetworkConfig}.
 * <p>
 * A channel-specific webhook takes priority over the global one. Channels listed in
 * {@link ru.fozeton.chatmanager.config.NetworkConfig#getIgnoreChannels()} are never sent. Requests are asynchronous.
 */
public class WebHooksHandler {
    private final HttpClient client = NetworkManager.getInstance().getClient();
    private final NetworkConfig networkConfig = ChatConfigManager.getInstance().getNetworkConfig();
    private final Logger log = new Logger(WebHooksHandler.class);
    private final Gson gson = new Gson();

    /**
     * Sends the message to the resolved webhook as a JSON body {@code {"content": "..."}}.
     * The content is plain text or a serialized component, depending on the webhook's {@code cleanText} setting.
     * Does nothing if the channel is ignored or no usable webhook is configured.
     * @param message the message to send
     */
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

    /** Returns {@code true} if the webhook exists, is enabled and has a non-blank URL. */
    private boolean isUsable(@Nullable NetworkConfig.WebHook webHook) {
        return webHook != null && webHook.isEnable() && !webHook.getUrl().isBlank();
    }

    /**
     * Picks the webhook URL: the channel webhook if usable, otherwise the global one.
     * @return the URL, or {@code null} if neither webhook is usable
     */
    @Nullable
    private String resolveWebhookUrl(@Nullable NetworkConfig.WebHook local, NetworkConfig.WebHook global) {
        if (isUsable(local)) return local.getUrl();
        if (isUsable(global)) return global.getUrl();
        return null;
    }

    /**
     * Picks the {@code cleanText} flag from the same webhook that {@link #resolveWebhookUrl} would choose.
     * @return the flag, or {@code false} if neither webhook is usable
     */
    private boolean resolveWebhookCleanText(@Nullable NetworkConfig.WebHook local, NetworkConfig.WebHook global) {
        if (isUsable(local)) return local.isCleanText();
        if (isUsable(global)) return global.isCleanText();
        return false;
    }

    private record NetworkMessage(String content) {
    }
}