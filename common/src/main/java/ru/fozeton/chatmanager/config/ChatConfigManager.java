package ru.fozeton.chatmanager.config;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatConfigManager {
    @Getter
    private static final ChatConfigManager instance = new ChatConfigManager();
    private final Map<Class<? extends IConfig>, ConfigHolder<? extends IConfig>> holders = new HashMap<>();
    private final Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    public synchronized <T extends IConfig> ConfigHolder<T> holder(Class<T> type) {
        return (ConfigHolder<T>) holders.computeIfAbsent(
                type,
                t -> AutoConfig.register(type, FolderJanksonSerializer::new)
        );
    }

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
            throw new RuntimeException(e);
        }
        return null;
    }

    public <T extends IConfig> T getOrLoad(Class<T> type) {
        return holder(type).getConfig();
    }

    public <T extends IConfig> void save(Class<T> type) {
        holder(type).save();
    }

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
}