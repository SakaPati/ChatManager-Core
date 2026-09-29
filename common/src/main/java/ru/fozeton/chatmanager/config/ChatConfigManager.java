package ru.fozeton.chatmanager.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatConfigManager {
    @Getter
    private static final ChatConfigManager instance = new ChatConfigManager();
    private final Map<Class<? extends IConfig>, ConfigHolder<? extends IConfig>> holders = new HashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized <T extends IConfig> ConfigHolder<T> holder(Class<T> type) {
        ConfigHolder<? extends IConfig> holder = holders.get(type);
        if (holder == null) {
            holder = AutoConfig.register(type, FolderJanksonSerializer::new);
            holders.put(type, holder);
        }
        return (ConfigHolder<T>) holder;
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