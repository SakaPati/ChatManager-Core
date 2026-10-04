package ru.fozeton.chatmanager;

import com.ferra13671.megaevents.eventbus.IEventBus;
import com.ferra13671.megaevents.eventbus.impl.EventBus;
import lombok.Getter;
import lombok.Setter;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.messages.ChatMessageParser;
import ru.fozeton.chatmanager.messages.DefaultMessage;
import ru.fozeton.chatmanager.network.NetworkManager;import ru.fozeton.chatmanager.network.WebHooks;import ru.fozeton.chatmanager.utils.DependencyLoader;
import ru.fozeton.chatmanager.utils.TickCounter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ChatManagerCore {
    public static final String MOD_ID = "chatmanager_core";
    public static final Pattern GIF_PATTERN = Pattern.compile(":((?=[A-Za-z0-9_-]*[A-Za-z])[A-Za-z0-9_-]{2,120}|\\d{15,19}):");    public static final IEventBus EVENT_BUS = new EventBus();
    @Getter
    private static final Map<String, ChatChannel> channels = new HashMap<>();
    @Getter
    @Setter
    private static ChatMessageParser messageParser = new DefaultMessage();
    private static Path configDir;
    private static Path gameDir;

    public static void init(Path configFolder) {
        configDir = configFolder.resolve("ChatManager-Core");
        gameDir = configFolder.getParent();
        DependencyLoader.loadDependencies(configDir.resolve("libs"));
        TickCounter.getInstance();
        registerChannel("Default", new ChatChannel("Default", "Основной"));
        NetworkManager.getInstance().setWebHooks(new WebHooks());
    }

    public static void registerChannel(String channelId, ChatChannel channel) {
        channels.put(channelId, channel);
    }

    public static void unregisterChannel(String channelId) {
        channels.remove(channelId);
    }

    public static Path getConfigDir() {
        if (configDir == null) throw new IllegalStateException("ChatManagerCore.init() не вызван");
        return configDir;
    }

    public static Path getGameDir() {
        if (gameDir == null) throw new IllegalStateException("ChatManagerCore.init() не вызван");
        return gameDir;
    }
}
