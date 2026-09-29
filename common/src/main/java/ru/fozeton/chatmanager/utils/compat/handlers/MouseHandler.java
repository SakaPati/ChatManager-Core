package ru.fozeton.chatmanager.utils.compat.handlers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MouseHandler {
    public static void handleClick(long window, int button, int action, int mods, double xpos, double ypos) {
        InputEvent.MouseInputEvent event = new InputEvent.MouseInputEvent(window, button, action, xpos, ypos);
        ChatManagerCore.EVENT_BUS.activate(event);
    }
}
