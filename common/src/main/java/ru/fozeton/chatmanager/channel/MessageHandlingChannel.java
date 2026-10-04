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

public class MessageHandlingChannel {
    @Getter
    private final List<MessageHandler> handlers = new ArrayList<>();

    public MessageHandlingChannel() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    public void registerHandler(MessageHandler handler) {
        handlers.add(handler);
    }

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

    protected boolean onPreProcess(Message message) {
        return true;
    }

    protected void onMessageModify(Message message) {
    }

    protected void onFilterMessage(Message message) {
    }

    protected void onPostProcess(Message message) {
    }

    protected void onMentionedProcess(Message message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && message.getFullPlain().contains("@%s".formatted(player.getName().getString()))) {
            message.setType(MessageType.MENTIONED);
            ChatManagerCore.EVENT_BUS.activate(new PlayerMentionedEvent(message, message.getAuthor()));
        }
    }

    protected void onNetworkDispatch(Message message) {
        NetworkManager.getInstance().dispatcher(message);
    }
}
