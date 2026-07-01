package ru.fozeton.chatmanager.mixin.chat;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.messages.Message;

@Mixin(ChatComponent.class)
public class AddedLocalMessage {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At(value = "HEAD"))
    public void addedMessage(Component component, CallbackInfo ci) {
        Message msg = ChatManagerCore.getMessageParser().parseAddedMessageLocalChat(component);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
    }
}
