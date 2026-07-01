package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.ClientTickEvent;

@Mixin(Minecraft.class)
public class ClientMixin {
    @Inject(at = @At(value = "HEAD"), method = "tick")
    public void modifyTick(CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.activate(new ClientTickEvent());
    }
}
