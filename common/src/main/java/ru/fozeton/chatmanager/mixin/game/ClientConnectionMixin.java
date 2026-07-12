package ru.fozeton.chatmanager.mixin.game;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.game.ClientLogin;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ClientConnectionMixin {
    @Inject(method = "finishLoginAndWaitForClient", at = @At(value = "HEAD"))
    public void onLogin(GameProfile gameProfile, CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.activate(new ClientLogin(gameProfile));
    }
}
