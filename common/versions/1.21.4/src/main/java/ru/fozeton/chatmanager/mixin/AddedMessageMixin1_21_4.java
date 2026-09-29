package ru.fozeton.chatmanager.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.messages.Message;import ru.fozeton.chatmanager.utils.compat.handlers.AddedMessageHandler;

@Mixin(ChatComponent.class)
public class AddedMessageMixin1_21_4 {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At(value = "HEAD"))
    public void addedMessage(Component component, CallbackInfo ci) {
        AddedMessageHandler.handleMessage(component);
    }
}
