package ru.fozeton.chatmanager.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;

@Mixin(KeyboardHandler.class)
public class KeyBoardMixin {
    @Inject(at = @At(value = "HEAD"), method = "keyPress")
    public void modifyOnKey(long window, int keyCode, int scancode, int action, int modifiers, CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.activate(new InputEvent.KeyInputEvent(
                keyCode,
                InputConstants.getKey(keyCode, scancode),
                action == 1 ? InputEvent.KeyInputEvent.Action.PRESS : action == 0 ? InputEvent.KeyInputEvent.Action.RELEASE : InputEvent.KeyInputEvent.Action.HOLDING
        ));
    }
}
