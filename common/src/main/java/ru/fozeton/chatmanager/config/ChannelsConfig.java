package ru.fozeton.chatmanager.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Config(name = "channels")
public class ChannelsConfig implements IConfig {
    private boolean isUseTimeFormatter = true;
    private String timeColor = "0xFF55FF55";
    private int lineBackgroundAlpha = 200;
    private int stripBackgroundAlpha = 255;
    private int sendMessageDelaySeconds = 1;

    @Setter(AccessLevel.NONE)
    @Comment("Global webhook is used to send ALL messages via HTTP")
    private WebHook globalWebHook = new WebHook();

    @ConfigEntry.Gui.Excluded
    private Map<String, ChannelSettings> channels = new LinkedHashMap<>();
    @ConfigEntry.Gui.CollapsibleObject
    private ChatHistory historyChat = new ChatHistory();

    @Override
    public void applyDefaults() {
        ChannelSettings defaultSettings = new ChannelSettings();
        defaultSettings.setId("Default");
        defaultSettings.setName("Main");
        defaultSettings.setX(15);
        defaultSettings.setY(360);
        defaultSettings.setWidth(400);
        defaultSettings.setHeight(200);

        channels.put("Default", defaultSettings);
    }

    @Getter
    @Setter
    public static class ChannelSettings {
        @NotNull
        private String id;
        @NotNull
        private String name;
        private boolean visible = true;
        @Nullable
        private String pattern;
        private int x;
        private int y;
        private int width;
        private int height;
        private int maxVisibleLine = 10;
        private String backgroundColor = "0x80000000";
        private String messageStackColor = "0xFFD3D3D3";
        private String blinkColor = "0xE63A2E1A";
        private String scrollColor = "0xFFCCCCCC";
        private int blinkDuration = 10000;
        private int blinkSpeed = 16;
        private boolean autoScroll = false;
        private boolean visibleScroll = true;
        private boolean useColorRemapper = false;
        private int scrollWidth = 2;
        private int fadingStartTime = 10000;
        private int fadingDuration = 1000;
        @ConfigEntry.Gui.CollapsibleObject
        private EditMode editMode = new EditMode();
        @ConfigEntry.Gui.CollapsibleObject
        @Comment("Local webhook takes priority over the global one. Even if the global webhook is enabled, the local one will be used for sending.")
        private WebHook webHook = new WebHook();
        @Comment("If true, messages from this channel will NEVER be sent to any webhook (neither local nor global).")
        private boolean isChannelIgnore = false;
    }

    @Getter
    @Setter
    public static class EditMode {
        private int markSize = 8;
        private String markColor = "0xFF7C6EF5";
        private String markHover = "0xFFB8AFF8";
    }

    @Getter
    @Setter
    public static class ChatHistory {
        private boolean useColorRemapper = true;
        private String backgroundColor = "0xFF000000";
        private String blinkColor = "0xE63A2E1A";
        private String scrollColor = "0xFF7c6ef5";
    }

    @Getter
    @Setter
    public static class WebHook {
        private String url = "";
        private boolean enable = false;

        @Setter(AccessLevel.NONE)
        @Comment("false (default) - sends a serialized Component (JSON with colors and formatting). true - sends plain text without formatting.")
        private boolean cleanText = false;
    }
}