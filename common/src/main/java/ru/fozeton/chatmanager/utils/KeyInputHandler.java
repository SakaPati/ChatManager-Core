package ru.fozeton.chatmanager.utils;

import com.ferra13671.megaevents.eventbus.IEventBus;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;
import ru.fozeton.chatmanager.events.game.DoublePressUpKeyEvent;
import ru.fozeton.chatmanager.utils.compat.providers.ScreenProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyInputHandler {
    private static long lastUpKeyPressTime = -1;

    public static void handleKey(long window, int keyCode, int scanCode, int action, int modifiers) {
        IEventBus eventBus = ChatManagerCore.EVENT_BUS;

        if (ScreenProvider.getScreen() instanceof ChatScreen && keyCode == GLFW.GLFW_KEY_UP &&
            action == GLFW.GLFW_PRESS) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpKeyPressTime <= 500) {
                eventBus.activate(new DoublePressUpKeyEvent());
                lastUpKeyPressTime = -1;
            } else {
                lastUpKeyPressTime = currentTime;
            }
        }

        InputEvent.KeyInputEvent.Action eventAction = switch (action) {
            case GLFW.GLFW_PRESS -> InputEvent.KeyInputEvent.Action.PRESS;
            case GLFW.GLFW_RELEASE -> InputEvent.KeyInputEvent.Action.RELEASE;
            default -> InputEvent.KeyInputEvent.Action.HOLDING;
        };

        InputConstants.Key key = (keyCode == -1)
                ? InputConstants.Type.SCANCODE.getOrCreate(scanCode)
                : InputConstants.Type.KEYSYM.getOrCreate(keyCode);

        InputEvent.KeyInputEvent event = new InputEvent.KeyInputEvent(
                window,
                keyCode,
                key,
                eventAction,
                modifiers
        );
        System.out.println(event);
        eventBus.activate(event);
    }
}