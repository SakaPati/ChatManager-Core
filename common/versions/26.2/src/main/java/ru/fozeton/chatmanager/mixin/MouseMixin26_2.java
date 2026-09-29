package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin26_2 {
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Inject(method = "onButton", at = @At("HEAD"))
    private void diamondchat$onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        ru.fozeton.chatmanager.utils.compat.handlers.MouseHandler.handleClick(
                handle,
                rawButtonInfo.button(),
                action,
                rawButtonInfo.modifiers(),
                this.xpos,
                this.ypos
        );
    }
}