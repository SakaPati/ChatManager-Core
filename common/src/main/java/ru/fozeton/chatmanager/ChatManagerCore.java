package ru.fozeton.chatmanager;

import com.ferra13671.megaevents.eventbus.IEventBus;
import com.ferra13671.megaevents.eventbus.impl.EventBus;
import dev.architectury.platform.Platform;
import lombok.Getter;
import lombok.Setter;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.ChatMessageParser;
import ru.fozeton.chatmanager.messages.DefaultMessage;
import ru.fozeton.chatmanager.utils.DependencyLoader;
import ru.fozeton.chatmanager.utils.TickCounter;

import java.nio.file.Path;
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
    public static final Path CONFIG_DIR = Platform.getConfigFolder().resolve("ChatManager-Core");

    public static void init() {
        DependencyLoader.loadDependencies(CONFIG_DIR.resolve("libs"));
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
