package ru.fozeton.chatmanager.mixin.chat;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.messages.Message;

@Mixin(ClientPacketListener.class)
public class ClientChatMixin {
    @Inject(method = "handlePlayerChat", at = @At(value = "HEAD"), cancellable = true)
    public void playerChatHandler(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        Message msg = ChatManagerCore.getMessageParser().parsePlayerChat(clientboundPlayerChatPacket);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
        ci.cancel();
    }

    @Inject(method = "handleSystemChat", at = @At(value = "HEAD"), cancellable = true)
    public void systemChatHandler(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        Message msg = ChatManagerCore.getMessageParser().parseSystemChat(clientboundSystemChatPacket);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
        ci.cancel();
    }
}