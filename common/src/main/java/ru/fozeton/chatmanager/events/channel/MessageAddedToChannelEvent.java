package ru.fozeton.chatmanager.events.channel;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.Message;

@Getter
@RequiredArgsConstructor
public class MessageAddedToChannelEvent extends Event<MessageAddedToChannelEvent> {
    final Message message;
    final ChatChannel channel;
}
