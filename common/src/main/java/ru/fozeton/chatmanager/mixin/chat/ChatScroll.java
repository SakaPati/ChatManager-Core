package ru.fozeton.chatmanager.mixin.chat;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.InputEvent;

@Mixin(ChatScreen.class)
public class ChatScroll {
    @Inject(method = "mouseScrolled", at = @At(value = "HEAD"))
    public void mousedScroll(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ChatManagerCore.EVENT_BUS.activate(new InputEvent.MouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount));
    }
}
