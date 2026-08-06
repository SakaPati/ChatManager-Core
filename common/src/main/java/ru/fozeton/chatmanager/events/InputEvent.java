package ru.fozeton.chatmanager.events;

import com.ferra13671.megaevents.event.Event;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

@Getter
public class InputEvent extends Event<InputEvent> {
    protected long window;
    protected int keyCode;

    @Getter
    public static class KeyInputEvent extends InputEvent {
        private final Action action;
        private final InputConstants.Key key;
        private final int modifiers;

        public KeyInputEvent(long window, int keyCode, InputConstants.Key key, Action action, int modifiers) {
            this.window = window;
            this.keyCode = keyCode;
            this.key = key;
            this.action = action;
            this.modifiers = modifiers;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public boolean isHoldingLeftShift() {
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        }

        public boolean isHoldingLeftControl() {
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
        }

        public boolean isHoldingLeftAlt() {
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
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

        public MouseInputEvent(long window, int button, int action, double mouseX, double mouseY) {
            this.window = window;
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