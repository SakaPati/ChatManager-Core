package ru.fozeton.chatmanager.events.channel;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChannelVisibilityChangedEvent extends Event<ChannelVisibilityChangedEvent> {
    private final String channelId;
    private final boolean visible;
}