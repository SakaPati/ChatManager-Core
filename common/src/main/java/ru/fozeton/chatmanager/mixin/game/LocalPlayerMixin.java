package ru.fozeton.chatmanager.mixin.game;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    public void onPlayerDeath(byte eventId, CallbackInfo ci) {
        if (eventId == 3) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            int x = (int) Math.floor(player.getX());
            int y = (int) Math.floor(player.getY());
            int z = (int) Math.floor(player.getZ());

            player.displayClientMessage(Component.literal(String.format("You death X: %s Y: %s Z: %s", x, y, z)).withColor(0xFFff0000), false);
        }
    }
}
