package ru.fozeton.chatmanager.events.speech;

import com.ferra13671.megaevents.event.Event;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public class VoskModelAbsentEvent extends Event<VoskModelAbsentEvent> {
    final Path path;
}
