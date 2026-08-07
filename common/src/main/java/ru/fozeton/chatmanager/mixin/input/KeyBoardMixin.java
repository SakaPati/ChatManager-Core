package ru.fozeton.chatmanager.mixin.input;

import com.ferra13671.megaevents.eventbus.IEventBus;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;
import ru.fozeton.chatmanager.events.game.DoublePressUpKeyEvent;

@Mixin(KeyboardHandler.class)
public class KeyBoardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private long chatmanager_core$lastUpKeyPressTime = -1;

    @Inject(at = @At(value = "HEAD"), method = "keyPress")
    public void modifyOnKey(long window, int keyCode, int scancode, int action, int modifiers, CallbackInfo ci) {
        IEventBus eventBus = ChatManagerCore.EVENT_BUS;

        if (minecraft.screen instanceof ChatScreen && keyCode == GLFW.GLFW_KEY_UP && action == GLFW.GLFW_PRESS) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - chatmanager_core$lastUpKeyPressTime <= 500) {
                eventBus.activate(new DoublePressUpKeyEvent());
                chatmanager_core$lastUpKeyPressTime = -1;
            } else chatmanager_core$lastUpKeyPressTime = currentTime;
        }

        InputEvent.KeyInputEvent.Action eventAction = action == 1 ? InputEvent.KeyInputEvent.Action.PRESS
                : action == 0 ? InputEvent.KeyInputEvent.Action.RELEASE
                : InputEvent.KeyInputEvent.Action.HOLDING;

        eventBus.activate(new InputEvent.KeyInputEvent(
                window,
                keyCode,
                InputConstants.getKey(keyCode, scancode),
                eventAction,
                modifiers
        ));
    }
}
