package ru.fozeton.chatmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.config.defaults.*;
import ru.fozeton.chatmanager.utils.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChatConfigManager {
    @Getter
    private static final ChatConfigManager instance = new ChatConfigManager();
    private final Logger log = new Logger(ChatConfigManager.class);
    private final Path configDir = Platform.getConfigFolder().resolve("ChatManager-Core");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Class<? extends IConfig>, IConfig> cache = new HashMap<>();
    private final Map<Class<? extends IConfig>, Default<?>> defaultFactories = new HashMap<>();

    private ChatConfigManager() {
        registerDefault(ChannelsConfig.class, new ChannelsDefault());
        registerDefault(MessagesFilterConfig.class, new MessagesFilterDefault());
        registerDefault(AiStyleTextConfig.class, new AiStyleTextDefault());
        registerDefault(AliasConfig.class, new AliasDefault());
        registerDefault(ColorRemapperConfig.class, new ColorRemapperDefault());
        registerDefault(MacrosConfig.class, new MacrosDefault());
    }

    public <T extends IConfig> void registerDefault(Class<T> configType, Default<T> factory) {
        if (cache.containsKey(configType)) {
            throw new IllegalStateException(configType.getSimpleName() + " is already loaded. It's too late to override the default.");
        }
        defaultFactories.put(configType, factory);
    }

    @SuppressWarnings("unchecked")
    public <T extends IConfig> T getOrLoad(Class<T> type) {
        return (T) cache.computeIfAbsent(type, t -> {
            Default<T> factory = (Default<T>) defaultFactories.get(t);

            if (factory != null) {
                String formattedName = type.getSimpleName()
                        .replace("Config", "")
                        .replaceAll("([A-Z])", " $1")
                        .trim()
                        .toLowerCase();

                String msg = "Creating default configuration for " + formattedName;

                T config = load(type);
                if (config == null || factory.isEmpty(config)) {
                    log.info(msg);
                    config = factory.createDefault();
                    save(config);
                }
                return config;
            } else throw new RuntimeException("Configuration not found for " + type);
        });
    }

    @Nullable
    private <T> T load(Class<T> type) {
        String fileName = type.getSimpleName().toLowerCase().replace("config", "") + ".json";
        Path file = configDir.resolve(fileName);
        try {
            if (Files.exists(file)) {
                return gson.fromJson(Files.readString(file), type);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T loadResource(Class<T> type, String modId, String path) {
        String fileName = type.getSimpleName().toLowerCase().replace("config", "") + ".json";
        try {
            Optional<Path> pathOptional = Platform.getMod(modId).findResource(path + fileName);

            if (pathOptional.isPresent()) {
                try (var reader = Files.newBufferedReader(pathOptional.get(), StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, type);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void save(Object config) {
        String fileName = config.getClass().getSimpleName().toLowerCase().replace("config", "") + ".json";
        Path file = configDir.resolve(fileName);
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ChannelsConfig getChannelsConfig() {
        return getOrLoad(ChannelsConfig.class);
    }

    public MessagesFilterConfig getMessagesFilterConfig() {
        return getOrLoad(MessagesFilterConfig.class);
    }

    public ColorRemapperConfig getRemapperConfig() {
        return getOrLoad(ColorRemapperConfig.class);
    }

    public AliasConfig getAliasConfig() {
        return getOrLoad(AliasConfig.class);
    }

    public AiStyleTextConfig getTextStyleConfig() {
        return getOrLoad(AiStyleTextConfig.class);
    }

    public MacrosConfig getMacrosConfig() {
        return getOrLoad(MacrosConfig.class);
    }
}