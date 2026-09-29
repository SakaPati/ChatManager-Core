package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.utils.compat.handlers.KeyInputHandler;

@Mixin(KeyboardHandler.class)
public class KeyBoardMixin1_21_4 {

    @Inject(at = @At("HEAD"), method = "keyPress")
    public void modifyOnKey(long window, int keyCode, int scancode, int action, int modifiers, CallbackInfo ci) {
        KeyInputHandler.handleKey(window, keyCode, scancode, action, modifiers);
    }
}