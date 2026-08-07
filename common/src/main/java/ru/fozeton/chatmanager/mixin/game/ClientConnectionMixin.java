package ru.fozeton.chatmanager.mixin.game;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.game.ClientLogin;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientConnectionMixin {
    @Inject(method = "handleLoginFinished", at = @At("HEAD"))
    private void onGameProfile(ClientboundLoginFinishedPacket clientboundLoginFinishedPacket, CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.activate(new ClientLogin());
    }
}
