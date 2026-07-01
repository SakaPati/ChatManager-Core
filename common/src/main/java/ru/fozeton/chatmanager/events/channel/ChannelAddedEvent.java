package ru.fozeton.chatmanager.events.channel;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.channel.ChatChannel;

@Getter
@RequiredArgsConstructor
public class ChannelAddedEvent extends Event<ChannelAddedEvent> {
    final ChatChannel channel;
}
