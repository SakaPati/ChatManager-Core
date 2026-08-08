package ru.fozeton.chatmanager.module;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import lombok.Getter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.events.game.SecondElapsedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ChatQueueManager {
    @Getter
    private static final ChatQueueManager instance = new ChatQueueManager();
    private final Queue<String> chunks = new LinkedList<>();
    private final ChannelsConfig channelsConfig = ChatConfigManager.getInstance().getChannelsConfig();
    @Nullable
    private ClientPacketListener connection;
    private int delay = 0;

    private ChatQueueManager() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    public void addMessage(ClientPacketListener connection, List<String> chunks) {
        this.connection = connection;
        this.chunks.addAll(chunks);
    }

    @EventSubscriber(event = SecondElapsedEvent.class)
    public void sendChunkMessage() {
        delay++;
        if (connection == null || chunks.isEmpty() || delay < channelsConfig.getSendMessageDelaySeconds()) return;

        String chunk = chunks.poll();
        if (chunk != null) {
            connection.sendChat(chunk);
            delay = 0;
        }
    }
}
