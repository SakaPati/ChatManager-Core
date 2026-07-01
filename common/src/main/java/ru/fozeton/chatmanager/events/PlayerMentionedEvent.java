package ru.fozeton.chatmanager.events;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.messages.Message;

@Getter
@RequiredArgsConstructor
public class PlayerMentionedEvent extends Event<PlayerMentionedEvent> {
    final Message message;
    @Nullable
    final String author;
}
