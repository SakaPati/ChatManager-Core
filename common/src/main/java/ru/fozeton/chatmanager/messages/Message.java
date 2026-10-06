package ru.fozeton.chatmanager.messages;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.metadata.MessageMetadata;
import ru.fozeton.chatmanager.messages.metadata.Metadata;
import ru.fozeton.chatmanager.utils.compat.providers.ClickEventProvider;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    @Getter(AccessLevel.NONE)
    private List<FormattedCharSequence> splitLines;
    @Getter(AccessLevel.NONE)
    private int splitWidth = -1;
    private MessageType type;
    private Component content;
    private Style style;
    private int stack = 1;

    public Message(
            UUID id,
            @Nullable String author,
            Component fullComponent,
            Component content,
            MessageType type,
            Instant timestamp
    ) {
        this.id = id;
        this.author = author;
        this.fullComponent = fullComponent;
        this.content = content;
        this.style = fullComponent.getStyle();
        this.type = type;
        this.timestamp = timestamp;
    }

    public Optional<ChatChannel> getChannel() {
        Optional<MessageMetadata> channelMetadata = metadata.get(MessageMetadata.class);
        return channelMetadata.map(MessageMetadata::getChannel);
    }

    public void setChannel(ChatChannel channel) {
        metadata.push(MessageMetadata.builder().channel(channel).build());
    }

    public void setClickEvent(ClickEvent.Action action, String value) {
        setClickEvent(action, value, null);
    }

    public void setContent(Component content) {
        this.content = content;
        this.style = content.getStyle();
        this.splitLines = null;
    }

    public void setClickEvent(ClickEvent.Action action, String value, @Nullable String commandPrefix) {
        this.content = ClickEventProvider.applyClickEvent(this.content, action, value, commandPrefix);
        this.style = this.content.getStyle();
    }

    public MutableComponent getMutContent() {
        return this.content.copy();
    }

    public String getFullPlain() {
        return fullComponent.getString();
    }

    public List<FormattedCharSequence> getSplitLines(Font font, int width) {
        if (splitLines == null || this.splitWidth != width) {
            splitLines = font.split(content, width).reversed();
            splitWidth = width;
        }

        return splitLines;
    }
}