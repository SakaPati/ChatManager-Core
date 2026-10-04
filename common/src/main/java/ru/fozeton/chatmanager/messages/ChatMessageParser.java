package ru.fozeton.chatmanager.messages;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.utils.compat.providers.HoverEventProvider;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public interface ChatMessageParser {
    default Message parsePlayerChat(ClientboundPlayerChatPacket packet) {
        Component content = packet.unsignedContent() != null
                ? packet.unsignedContent()
                : packet.chatType().decorate(Component.literal(packet.body().content()));

        return new Message(
                UUID.randomUUID(),
                packet.chatType().name().getString(),
                useTimeFormatter(content),
                MessageType.PLAYER,
                packet.body().timeStamp()
        );
    }

    default Message parseSystemChat(ClientboundSystemChatPacket packet) {
        Component content = packet.content();
        TextColor textColor = content.getStyle().getColor();
        MessageType type = MessageType.SYSTEM;
        if (textColor != null) {
            String colorName = textColor.serialize();
            boolean isRedError = "red".equals(colorName) || "dark_red".equals(colorName);
            if (isRedError) type = MessageType.ERROR;
        }

        return new Message(
                UUID.randomUUID(),
                null,
                useTimeFormatter(content),
                type,
                Instant.now()
        );
    }

    default Message parseAddedMessageLocalChat(Component component) {
        return new Message(
                UUID.randomUUID(),
                null,
                component,
                MessageType.CLIENT,
                Instant.now()
        );
    }

    default Component useTimeFormatter(Component content) {
        ChannelsConfig channelsConfig = ChatConfigManager.getInstance().getChannelsConfig();
        if (!channelsConfig.isUseTimeFormatter()) return content;

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fullDate = new SimpleDateFormat("HH:mm:ss.SSS dd.MM.yyyy").format(new Date());
        int color = Long.decode(channelsConfig.getTimeColor()).intValue();

        Component timePart = Component.literal(time + " ")
                .withColor(color)
                .withStyle(style -> style.withHoverEvent(HoverEventProvider.showText(Component.literal(fullDate))));

        return Component.empty().append(timePart).append(content);
    }
}
