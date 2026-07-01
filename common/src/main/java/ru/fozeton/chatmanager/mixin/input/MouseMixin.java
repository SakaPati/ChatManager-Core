package ru.fozeton.chatmanager.mixin.input;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Inject(method = "onPress", at = @At("HEAD"))
    public void modifyOnMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        InputEvent.MouseInputEvent event = new InputEvent.MouseInputEvent(button, action, xpos, ypos);
        ChatManagerCore.EVENT_BUS.activate(event);
    }
}