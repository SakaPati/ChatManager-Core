package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface ILocalPlayer {
    @Nullable
    static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    void displayClientMessage(Component component, boolean overlay);
}
