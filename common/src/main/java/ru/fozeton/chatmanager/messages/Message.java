package ru.fozeton.chatmanager.messages;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.utils.compat.providers.ClickEventProvider;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class Message {
    private final UUID id;
    @Nullable
    private final String author;
    private final Component fullComponent;
    private final Instant timestamp;
    private final Metadata metadata = new Metadata();
    private MessageType type;
    private Component content;
    private Style style;
    private int stack = 1;

    public Message(
            UUID id,
            @Nullable String author,
            Component fullComponent,
            MessageType type,
            Instant timestamp
    ) {

        this.id = id;
        this.author = author;
        this.fullComponent = fullComponent;
        this.content = this.fullComponent;
        this.style = fullComponent.getStyle();
        this.type = type;
        this.timestamp = timestamp;
    }

    public ChatChannel getChannel() {
        return metadata.getMetadata(ChannelMetadata.class).orElseThrow().getChannel();
    }

    public void setChannel(ChatChannel channel) {
        metadata.pushMetadata(new ChannelMetadata(channel));
    }

    public void setClickEvent(ClickEvent.Action action, String value) {
        setClickEvent(action, value, null);
    }

    public void setClickEvent(ClickEvent.Action action, String value, @Nullable String commandPrefix) {
        this.content = ClickEventProvider.applyClickEvent(this.content, action, value, commandPrefix);
        this.style = this.content.getStyle();
    }

    public MutableComponent getMutContent() {
        return this.fullComponent.copy();
    }

    public String getFullPlain() {
        return fullComponent.toString();
    }
}