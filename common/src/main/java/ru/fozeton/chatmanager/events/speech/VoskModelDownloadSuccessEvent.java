package ru.fozeton.chatmanager.events.speech;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.module.SpeechToText;

@Getter
@RequiredArgsConstructor
public class VoskModelDownloadSuccessEvent extends Event<VoskModelDownloadSuccessEvent> {
    private final SpeechToText.Language language;
}
