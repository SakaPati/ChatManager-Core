package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ChannelsConfig {
    private boolean isUseTimeFormatter = true;
    private String timeColor = "0xFF55FF55";
    private int lineBackgroundAlpha = 200;
    private int stripBackgroundAlpha = 255;
    private WebHook globalWebHook = new WebHook();
    private Map<String, ChannelSettings> channels = new LinkedHashMap<>();
    private ChatHistory historyChat = new ChatHistory();

    protected ChannelsConfig createDefault() {
        return new ChannelsConfig();
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
        private String backgroundColor = "0xE618181F";
        private String blinkColor = "0xE63A2E1A";
        private String scrollColor = "0xFF7C6EF5";
        private int blinkDuration = 10000;
        private int blinkSpeed = 16;
        private boolean autoScroll = false;
        private boolean visibleScroll = true;
        private boolean useColorRemapper = true;
        private int scrollWidth = 2;
        private int fadingStartTime = 10000;
        private int fadingDuration = 1000;
        private EditMode editMode = new EditMode();
        private WebHook webHook = new WebHook();
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
        private String backgroundColor = "0xE618181F";
        private String blinkColor = "0xE63A2E1A";
        private String scrollColor = "0xFF7C6EF5";
    }

    @Getter
    @Setter
    public static class WebHook {
        private String url = "";
        private boolean enable = false;
        private boolean cleanText = false;
    }
}