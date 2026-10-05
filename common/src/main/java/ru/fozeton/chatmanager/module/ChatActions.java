package ru.fozeton.chatmanager.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.providers.LocalPlayerProvider;

public class ChatActions {
    private final LocalPlayer player = Minecraft.getInstance().player;

    protected void sendChat(String message) {
        if (player != null) player.connection.sendChat(message);
    }

    protected void sendCommand(String command) {
        if (player != null) player.connection.sendCommand(command);
    }

    protected void sendLocalMessage(Component message, boolean overlay) {
        LocalPlayerProvider.displayClientMessage(message, overlay);
    }
}
