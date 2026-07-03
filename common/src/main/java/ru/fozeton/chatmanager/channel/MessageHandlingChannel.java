package ru.fozeton.chatmanager.channel;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import com.google.gson.Gson;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.events.PlayerMentionedEvent;
import ru.fozeton.chatmanager.events.channel.MessageStackEvent;
import ru.fozeton.chatmanager.messages.Message;
import ru.fozeton.chatmanager.messages.MessageHandler;
import ru.fozeton.chatmanager.messages.MessageType;
import ru.fozeton.chatmanager.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MessageHandlingChannel {
    protected final ChannelsConfig channelsConfig = ChatConfigManager.getInstance().getChannelsConfig();
    private final Logger log = new Logger(MessageHandlingChannel.class);
    private final Gson gson = new Gson();
    @Getter
    private final List<MessageHandler> handlers = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();

    public MessageHandlingChannel() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    public void registerHandler(MessageHandler handler) {
        handlers.add(handler);
    }

    @EventSubscriber(event = MessageReceivedEvent.class)
    public void handle(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (!onPreProcess(message)) return;
        onMessageModify(message);
        onFilterMessage(message);
        onPostProcess(message);
        onMentionedProcess(message);
        onNetworkDispatch(message);
    }

    @EventSubscriber(event = MessageStackEvent.class)
    public void onStackMessage(MessageStackEvent event) {

    }

    protected boolean onPreProcess(Message message) {
        return true;
    }

    protected void onMessageModify(Message message) {
    }

    protected void onFilterMessage(Message message) {
    }

    protected void onPostProcess(Message message) {
    }

    protected void onMentionedProcess(Message message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && message.getPlainText().contains("@%s".formatted(player.getName().getString()))) {
            message.setType(MessageType.MENTIONED);
            ChatManagerCore.EVENT_BUS.activate(new PlayerMentionedEvent(message, message.getAuthor()));
        }
    }

    protected void onNetworkDispatch(Message message) {
        ClientLevel level = Minecraft.getInstance().level;
        ChatChannel messageChannel = message.getChannel();
        if (level == null) return;
        ChannelsConfig.WebHook localWebHook = null;
        ChannelsConfig.WebHook globalWebHook = channelsConfig.getGlobalWebHook();
        if (messageChannel != null) {
            ChannelsConfig.ChannelSettings channelSettings = channelsConfig.getChannels().get(messageChannel.getId());
            if (channelSettings != null) {
                if (channelSettings.isChannelIgnore()) return;
                localWebHook = channelSettings.getWebHook();
            }
        }

        String targetUrl = resolveWebhookUrl(localWebHook, globalWebHook);
        if (targetUrl == null) return;

        try {
            String payload = resolveWebhookCleanText(localWebHook, globalWebHook) ? message.getPlainText() : Component.Serializer.toJson(message.getContent(), level.registryAccess());
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

    @Nullable
    private String resolveWebhookUrl(ChannelsConfig.WebHook local, ChannelsConfig.WebHook global) {
        if (local != null && local.isEnable() && !local.getUrl().isBlank()) return local.getUrl();
        else if (global.isEnable() && !global.getUrl().isBlank()) return global.getUrl();
        return null;
    }

    private boolean resolveWebhookCleanText(ChannelsConfig.WebHook local, ChannelsConfig.WebHook global) {
        if (local != null && local.isEnable() && !local.getUrl().isBlank()) return local.isCleanText();
        else if (global.isEnable() && !global.getUrl().isBlank()) return global.isCleanText();
        return false;
    }

    private record NetworkMessage(String content) {
    }
}
