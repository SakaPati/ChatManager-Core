package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.ChannelsConfig;

public class ChannelsDefault implements Default<ChannelsConfig> {
    @Override
    public ChannelsConfig createDefault() {
        ChannelsConfig config = new ChannelsConfig();

        ChannelsConfig.ChannelSettings defaultSettings = new ChannelsConfig.ChannelSettings();
        defaultSettings.setId("Default");
        defaultSettings.setName("Main");
        defaultSettings.setX(15);
        defaultSettings.setY(360);
        defaultSettings.setWidth(400);
        defaultSettings.setHeight(200);

        config.getChannels().put("Default", defaultSettings);

        return config;
    }

    @Override
    public boolean isEmpty(ChannelsConfig config) {
        return config == null || config.getChannels() == null || config.getChannels().isEmpty();
    }
}