package ru.fozeton.chatmanager.events.channel;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.Message;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class MessageStackEvent extends Event<MessageStackEvent> {
    private final UUID id;
    private final Message message;
    private final ChatChannel channel;
}
