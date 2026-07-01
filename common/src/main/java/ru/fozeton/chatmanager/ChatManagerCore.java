package ru.fozeton.chatmanager;

import com.ferra13671.megaevents.eventbus.IEventBus;
import com.ferra13671.megaevents.eventbus.impl.EventBus;
import lombok.Getter;
import lombok.Setter;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.ChatMessageParser;
import ru.fozeton.chatmanager.messages.DefaultMessage;
import ru.fozeton.chatmanager.utils.TickCounter;

import java.util.HashMap;
import java.util.Map;

public final class ChatManagerCore {
    public static final String MOD_ID = "chatmanager_core";
    public static final IEventBus EVENT_BUS = new EventBus();
    @Getter
    private static final Map<String, ChatChannel> channels = new HashMap<>();
    @Getter
    @Setter
    private static ChatMessageParser messageParser = new DefaultMessage();

    public static void init() {
        TickCounter.getInstance();
        registerChannel("Default", new ChatChannel("Default", "Основной"));
    }

    public static void registerChannel(String channelId, ChatChannel channel) {
        channels.put(channelId, channel);
    }

    public static void unregisterChannel(String channelId) {
        channels.remove(channelId);
    }
}
