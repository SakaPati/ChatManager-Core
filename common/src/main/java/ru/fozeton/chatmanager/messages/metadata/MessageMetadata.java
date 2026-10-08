package ru.fozeton.chatmanager.messages.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.fozeton.chatmanager.channel.ChatChannel;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class MessageMetadata implements MetadataType {
    private final ChatChannel channel;
    private final Integer lineColor;
    private final Integer borderColor;
}