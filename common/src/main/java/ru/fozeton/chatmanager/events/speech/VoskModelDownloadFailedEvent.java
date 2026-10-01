package ru.fozeton.chatmanager.events.speech;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.module.SpeechToText;

@Getter
@RequiredArgsConstructor
public class VoskModelDownloadFailedEvent extends Event<VoskModelDownloadFailedEvent> {
    private final SpeechToText.Language language;
    private final Exception exception;
}
