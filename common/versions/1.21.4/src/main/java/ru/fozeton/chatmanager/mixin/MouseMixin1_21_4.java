package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin1_21_4 {
    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Inject(method = "onPress", at = @At("HEAD"))
    public void modifyOnMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        ru.fozeton.chatmanager.utils.compat.handlers.MouseHandler.handleClick(window, button, action, mods, xpos, ypos);
    }
}