package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.utils.compat.handlers.KeyInputHandler;

@Mixin(KeyboardHandler.class)
public class KeyBoardMixin26_2 {

    @Inject(at = @At("HEAD"), method = "keyPress")
    public void modifyOnKey(long window, int action, KeyEvent event, CallbackInfo ci) {
        KeyInputHandler.handleKey(
                window,
                event.key(),
                event.scancode(),
                action,
                event.modifiers()
        );
    }
}