package ru.fozeton.chatmanager.events;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.messages.Message;

@Getter
@RequiredArgsConstructor
public class MessageReceivedEvent extends Event<MessageReceivedEvent> {
    private final Message message;
}