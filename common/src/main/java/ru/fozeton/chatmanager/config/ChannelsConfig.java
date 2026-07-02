package ru.fozeton.chatmanager.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ChannelsConfig implements IConfig{
    private boolean isUseTimeFormatter = true;
    private String timeColor = "0xFF55FF55";
    private int lineBackgroundAlpha = 200;
    private int stripBackgroundAlpha = 255;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final String _globalWebHook_ = "Глобальный хук используется для отправки ВСЕХ сообщений по http";
    private WebHook globalWebHook = new WebHook();
    private Map<String, ChannelSettings> channels = new LinkedHashMap<>();
    private ChatHistory historyChat = new ChatHistory();

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
        private EditMode editMode = new EditMode();
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private final String _webHook_ = "Локальный хук имеет приоритет над глобальным, даже если глобальный включен, то для отправки будет использоваться локальный";
        private WebHook webHook = new WebHook();
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private final String _isChannelIgnore_ = "Если true - сообщения этого канала НИКОГДА не отправляются на webhook (ни на локальный, ни на глобальный), независимо от настроек webHook и globalWebHook.";
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
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private final String _cleanText_ = "Режим отправки текста в webhook. false (по умолчанию) - отправляется сериализованный Component (JSON с цветами и форматированием). true - отправляется чистый текст без форматирования.";
        private boolean cleanText = false;
    }
}