package ru.fozeton.chatmanager.events.game;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StylizeMessageEvent extends Event<StylizeMessageEvent> {
    private final String style;
}
