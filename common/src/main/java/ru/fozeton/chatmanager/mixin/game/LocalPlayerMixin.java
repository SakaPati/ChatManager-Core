package ru.fozeton.chatmanager.mixin.game;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.game.PlayerDeathEvent;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    public void onPlayerDeath(byte eventId, CallbackInfo ci) {
        if (eventId == 3) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            double x = Math.floor(player.getX());
            double y = Math.floor(player.getY());
            double z = Math.floor(player.getZ());

            ChatManagerCore.EVENT_BUS.activate(new PlayerDeathEvent(player, x, y, z));
        }
    }
}
