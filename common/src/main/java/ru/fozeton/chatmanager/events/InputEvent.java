package ru.fozeton.chatmanager.events;

import com.ferra13671.megaevents.event.Event;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;

@Getter
public class InputEvent extends Event<InputEvent> {
    protected int keyCode;

    @Getter
    public static class KeyInputEvent extends InputEvent {
        private final Action action;
        private final InputConstants.Key key;

        public KeyInputEvent(int keyCode, InputConstants.Key key, Action action) {
            this.keyCode = keyCode;
            this.key = key;
            this.action = action;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public enum Action {
            PRESS,
            RELEASE,
            HOLDING
        }
    }

    @Getter
    public static class MouseInputEvent extends InputEvent {
        private final int button;
        private final int action;
        private final double mouseX;
        private final double mouseY;

        public MouseInputEvent(int button, int action, double mouseX, double mouseY) {
            this.button = button;
            this.action = action;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

    @Getter
    public static class MouseScrollEvent extends InputEvent {
        private final double mouseX, mouseY, horizontal, vertical;

        public MouseScrollEvent(double mouseX, double mouseY, double horizontal, double vertical) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }
}