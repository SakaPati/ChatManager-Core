package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static ru.fozeton.chatmanager.config.IConfig.parseColor;

@Getter
@Setter
@Config(name = "channels")
public class ChannelsConfig implements IConfig {
    private boolean isUseTimeFormatter = true;
    private String timeColor = "0xFF55FF55";
    private int lineBackgroundAlpha = 200;
    private int stripBackgroundAlpha = 255;
    private int sendMessageDelaySeconds = 1;

    @ConfigEntry.Gui.Excluded
    private Map<String, ChannelSettings> channels = new LinkedHashMap<>();
    @ConfigEntry.Gui.CollapsibleObject
    private ChatHistory historyChat = new ChatHistory();

    public int getTimeColor() {
        return parseColor(timeColor, 0xFF55FF55);
    }

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

        public int getBackgroundColor() {
            return parseColor(backgroundColor, 0x80000000);
        }

        public int getMessageStackColor() {
            return parseColor(messageStackColor, 0xFFD3D3D3);
        }

        public int getBlinkColor() {
            return parseColor(blinkColor, 0xE63A2E1A);
        }

        public int getScrollColor() {
            return parseColor(scrollColor, 0xFFCCCCCC);
        }
    }

    @Getter
    @Setter
    public static class EditMode {
        private int markSize = 8;
        private String markColor = "0xFF7C6EF5";
        private String markHover = "0xFFB8AFF8";

        public int getMarkColor() {
            return parseColor(markColor, 0xFF7C6EF5);
        }

        public int getMarkHover() {
            return parseColor(markHover, 0xFFB8AFF8);
        }
    }

    @Getter
    @Setter
    public static class ChatHistory {
        private boolean useColorRemapper = true;
        private String backgroundColor = "0xFF000000";
        private String blinkColor = "0xE63A2E1A";
        private String scrollColor = "0xFF7c6ef5";

        public int getBackgroundColor() {
            return parseColor(backgroundColor, 0xFF000000);
        }

        public int getBlinkColor() {
            return parseColor(blinkColor, 0xE63A2E1A);
        }

        public int getScrollColor() {
            return parseColor(scrollColor, 0xFF7C6EF5);
        }
    }
}