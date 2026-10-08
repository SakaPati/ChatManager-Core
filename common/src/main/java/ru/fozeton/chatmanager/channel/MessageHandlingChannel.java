package ru.fozeton.chatmanager.channel;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.events.PlayerMentionedEvent;
import ru.fozeton.chatmanager.messages.Message;
import ru.fozeton.chatmanager.messages.MessageHandler;
import ru.fozeton.chatmanager.messages.MessageType;
import ru.fozeton.chatmanager.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing pipeline for incoming chat messages.
 * <p>
 * Listens for {@link MessageReceivedEvent} and runs each message through a fixed sequence of steps:
 * pre-process (can cancel), modify, filter, post-process, mention detection and network dispatch.
 * Subclasses override the {@code on*} methods to customize the steps.
 */
public class MessageHandlingChannel {
    /** Registered message handlers. */
    @Getter
    private final List<MessageHandler> handlers = new ArrayList<>();

    /** Creates the pipeline and subscribes it to the event bus. */
    public MessageHandlingChannel() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    /**
     * Registers a message handler.
     *
     * @param handler the handler to add
     */
    public void registerHandler(MessageHandler handler) {
        handlers.add(handler);
    }

    /**
     * Runs the received message through all processing steps in order.
     * Stops early if {@link #onPreProcess(Message)} returns {@code false}.
     *
     * @param event the received message event
     */
    @EventSubscriber(event = MessageReceivedEvent.class)
    public void handle(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (!onPreProcess(message)) return;
        onMessageModify(message);
        onFilterMessage(message);
        onPostProcess(message);
        onMentionedProcess(message);
        onNetworkDispatch(message);
    }

    /**
     * First step, runs before any changes to the message.
     *
     * @param message the incoming message
     * @return {@code false} to drop the message and skip all further steps, {@code true} to continue
     */
    protected boolean onPreProcess(Message message) {
        return true;
    }

    /**
     * Step for changing the message content or properties. Does nothing by default.
     *
     * @param message the message to modify
     */
    protected void onMessageModify(Message message) {
    }

    /**
     * Step for filtering the message, for example hiding or marking unwanted text. Does nothing by default.
     *
     * @param message the message to filter
     */
    protected void onFilterMessage(Message message) {
    }

    /**
     * Step that runs after modification and filtering. Does nothing by default.
     *
     * @param message the processed message
     */
    protected void onPostProcess(Message message) {
    }

    /**
     * Checks whether the local player is mentioned as {@code @name}. If so, marks the message as
     * {@link MessageType#MENTIONED} and publishes a {@link PlayerMentionedEvent}.
     *
     * @param message the message to check
     */
    protected void onMentionedProcess(Message message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && message.getFullPlain().contains("@%s".formatted(player.getName().getString()))) {
            message.setType(MessageType.MENTIONED);
            ChatManagerCore.EVENT_BUS.activate(new PlayerMentionedEvent(message, message.getAuthor()));
        }
    }

    /**
     * Sends the message to the network layer (webhooks).
     *
     * @param message the final message
     */
    protected void onNetworkDispatch(Message message) {
        NetworkManager.getInstance().dispatcher(message);
    }
}