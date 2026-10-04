package ru.fozeton.chatmanager.messages;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.fozeton.chatmanager.channel.ChatChannel;

@Getter
@EqualsAndHashCode(callSuper = false)
public class ChannelMetadata extends Metadata {
    private final ChatChannel channel;

    public ChannelMetadata(ChatChannel channel) {
        this.channel = channel;
    }
}
