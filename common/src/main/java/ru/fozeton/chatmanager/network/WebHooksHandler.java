package ru.fozeton.chatmanager.network;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.messages.Message;
import ru.fozeton.chatmanager.utils.Logger;
import ru.fozeton.chatmanager.utils.compat.providers.ComponentSerializerProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebHooksHandler {
    private final HttpClient client = NetworkManager.getInstance().getClient();
    private final ChannelsConfig channelsConfig = ChatConfigManager.getInstance().getChannelsConfig();
    private final Logger log = new Logger(WebHooksHandler.class);
    private final Gson gson = new Gson();

    protected void onWebHook(Message message) {
        ChatChannel messageChannel = message.getChannel();
        ChannelsConfig.WebHook localWebHook = null;
        ChannelsConfig.WebHook globalWebHook = channelsConfig.getGlobalWebHook();
        if (messageChannel != null) {
            ChannelsConfig.ChannelSettings channelSettings = channelsConfig.getChannels().get(messageChannel.getId());
            if (channelSettings != null && !channelSettings.isChannelIgnore()) {
                localWebHook = channelSettings.getWebHook();
            }
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

    private boolean isUsable(@Nullable ChannelsConfig.WebHook webHook) {
        return webHook != null && webHook.isEnable() && !webHook.getUrl().isBlank();
    }

    @Nullable
    private String resolveWebhookUrl(@Nullable ChannelsConfig.WebHook local, ChannelsConfig.WebHook global) {
        if (isUsable(local)) return local.getUrl();
        if (isUsable(global)) return global.getUrl();
        return null;
    }

    private boolean resolveWebhookCleanText(@Nullable ChannelsConfig.WebHook local, ChannelsConfig.WebHook global) {
        if (isUsable(local)) return local.isCleanText();
        if (isUsable(global)) return global.isCleanText();
        return false;
    }

    private record NetworkMessage(String content) {
    }
}
