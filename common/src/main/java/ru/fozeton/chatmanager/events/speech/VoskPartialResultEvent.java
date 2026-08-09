package ru.fozeton.chatmanager.events.speech;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VoskPartialResultEvent extends Event<VoskPartialResultEvent> {
    private final String partialResult;
}
