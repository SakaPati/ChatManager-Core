package ru.fozeton.chatmanager.config;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.exceptions.ConfigException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that manages all mod configs.
 * <p>
 * Configs are registered lazily through AutoConfig on first access and cached by their class,
 * so every config type has exactly one {@link ConfigHolder}. Also provides loading of
 * bundled JSON resources and typed shortcuts for every config used by the mod.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatConfigManager {
    @Getter
    private static final ChatConfigManager instance = new ChatConfigManager();
    private final Map<Class<? extends IConfig>, ConfigHolder<? extends IConfig>> holders = new HashMap<>();
    private final Gson gson = new Gson();

    /**
     * Returns the holder of the given config type, registering it on first use.
     *
     * @param type the config class
     * @param <T>  the config type
     * @return the cached or newly registered holder
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends IConfig> ConfigHolder<T> holder(Class<T> type) {
        return (ConfigHolder<T>) holders.computeIfAbsent(
                type,
                t -> AutoConfig.register(type, FolderJanksonSerializer::new)
        );
    }

    /**
     * Loads a JSON file bundled inside the mod (classpath resource) and deserializes it with Gson.
     * <p>
     * The file name is derived from the class name: lowercased, with {@code "config"} removed,
     * plus the {@code .json} extension (e.g. {@code ChannelsConfig} becomes {@code channels.json}).
     *
     * @param type the class to deserialize into
     * @param path the resource directory prefix, including the trailing slash
     * @param <T>  the resulting type
     * @return the deserialized object, or {@code null} if the resource does not exist
     * @throws ConfigException if the resource cannot be read
     */
    @Nullable
    public <T> T loadResource(Class<T> type, String path) {
        String fileName = type.getSimpleName().toLowerCase().replace("config", "") + ".json";
        String fullPath = path + fileName;
        try (var stream = getClass().getClassLoader().getResourceAsStream(fullPath)) {
            if (stream != null) {
                try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    return gson.fromJson(reader, type);
                }
            }
        } catch (IOException e) {
            throw new ConfigException(e);
        }
        return null;
    }

    /**
     * Returns the current instance of the given config, registering and loading it if needed.
     *
     * @param type the config class
     * @param <T>  the config type
     * @return the loaded config
     */
    public <T extends IConfig> T getOrLoad(Class<T> type) {
        return holder(type).getConfig();
    }

    /**
     * Writes the current state of the given config to disk.
     *
     * @param type the config class
     * @param <T>  the config type
     */
    public <T extends IConfig> void save(Class<T> type) {
        holder(type).save();
    }

    /**
     * Re-reads the given config from disk, discarding unsaved in-memory changes.
     *
     * @param type the config class
     * @param <T>  the config type
     */
    public <T extends IConfig> void reload(Class<T> type) {
        holder(type).load();
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

    public MathConfig getMathConfig() {
        return getOrLoad(MathConfig.class);
    }

    public VoiceConfig getVoiceConfig() {
        return getOrLoad(VoiceConfig.class);
    }

    public NetworkConfig getNetworkConfig() {
        return getOrLoad(NetworkConfig.class);
    }
}