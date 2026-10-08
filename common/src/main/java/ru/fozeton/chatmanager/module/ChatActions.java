package ru.fozeton.chatmanager.module;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.NetworkConfig;
import ru.fozeton.chatmanager.events.network.socket.SocketPacketReceivedEvent;
import ru.fozeton.chatmanager.exceptions.ExecutingUnauthorizedCommandException;
import ru.fozeton.chatmanager.exceptions.UnknownPacketException;
import ru.fozeton.chatmanager.network.sockets.*;
import ru.fozeton.chatmanager.utils.Logger;
import ru.fozeton.chatmanager.utils.compat.providers.ComponentSerializerProvider;
import ru.fozeton.chatmanager.utils.compat.providers.LocalPlayerProvider;

import java.util.List;

/**
 * Executes actions requested by the backend through socket packets:
 * sending chat messages, running commands and showing local messages.
 * <p>
 * Packets are handled on the Minecraft main thread, since chat and rendering must not be
 * touched from network threads.
 */
public class ChatActions {
    @Getter
    private static final ChatActions instance = new ChatActions();
    private final Logger log = new Logger(ChatActions.class);
    private final NetworkConfig networkCfg = ChatConfigManager.getInstance().getNetworkConfig();

    private ChatActions() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    /**
     * Receives a decoded socket packet and dispatches it to the matching action
     * on the Minecraft main thread.
     *
     * @throws UnknownPacketException if the packet type is not supported
     */
    @EventSubscriber(event = SocketPacketReceivedEvent.class)
    public void packetHandler(SocketPacketReceivedEvent event) {
        Minecraft.getInstance().execute(() -> {
            SocketPacket packet = event.getPacket();

            switch (packet) {
                case ChatSendPacket(String text) -> sendChat(text);
                case ChatCommandPacket(String command) -> sendCommand(command);
                case ChatAddPacket(String text, String json, boolean overlay) -> {
                    Component parsed = ComponentSerializerProvider.fromJson(json);
                    sendLocalMessage(Component.literal(text), parsed, overlay);
                }
                case CustomPacket(String type, JsonObject data) -> customPacket(type, data);
                default -> throw new UnknownPacketException("Unexpected value: " + packet);
            }
        });
    }

    /**
     * Sends a chat message as the local player. Does nothing if no player is present.
     *
     * @param message the text to send
     */
    protected void sendChat(String message) {
        if (player() != null) player().connection.sendChat(message);
    }

    /**
     * Executes a command as the local player, but only if it is present in
     * {@link NetworkConfig#getAllowedCommands()} (exact match).
     * Blocked commands are logged and reported to the player in chat.
     *
     * @param command the command to run
     */
    private void sendCommand(String command) {
        try {
            List<String> allowedCommands = networkCfg.getAllowedCommands();
            if (allowedCommands.isEmpty() || !allowedCommands.contains(command)) {
                throw new ExecutingUnauthorizedCommandException(
                        """
                                Command '%s' is not allowed. \
                                Add it to the allowed commands list in the network config to execute it.""".formatted(
                                command)
                );
            }
            if (player() != null) player().connection.sendCommand(command);
        } catch (ExecutingUnauthorizedCommandException e) {
            log.warn("Blocked unauthorized command: " + command);
            LocalPlayerProvider.displayClientMessage(
                    Component.literal("§cBlocked unauthorized command: " + command),
                    false
            );
        }
    }

    private LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    /**
     * Displays a message in the local chat or action bar. Not sent to the server.
     *
     * @param message  the preferred (formatted) component, may be {@code null}
     * @param fallback the component to show if {@code message} is {@code null}
     * @param overlay  {@code true} to show in the action bar, {@code false} for the chat
     */
    protected void sendLocalMessage(Component message, Component fallback, boolean overlay) {
        LocalPlayerProvider.displayClientMessage(message != null ? message : fallback, overlay);
    }

    /**
     * Hook for handling {@link CustomPacket}s. Does nothing by default.
     *
     * @param type the custom packet type
     * @param data the raw packet payload
     */
    protected void customPacket(String type, JsonObject data) {
    }
}
