package ru.fozeton.chatmanager.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Config(name = "network")
public class NetworkConfig implements IConfig {

    @Setter(AccessLevel.NONE)
    @ConfigEntry.Gui.Excluded
    @Comment("Global webhook: same as a channel webhook, but for ALL channels. A channel's own enabled webhook takes priority.")
    private WebHook globalWebHook = new WebHook();

    @Setter(AccessLevel.NONE)
    @Comment("WebSocket address of your backend, e.g. wss://example.com/chatmanager. Empty = disabled.")
    private String socketUrl = "";

    @ConfigEntry.Gui.Excluded
    @Comment("Webhooks per channel. Key = channel id from channels.json5. If set and enabled, it is used instead of the global one.")
    private Map<String, WebHook> channelsWebHooks = new LinkedHashMap<>();

    @ConfigEntry.Gui.Excluded
    @Comment("Channel ids that are NEVER sent to any webhook. Example: [\"Default\"]")
    private List<String> ignoreChannels = new ArrayList<>();

    @Getter
    @Setter
    public static class WebHook {
        @Comment("Webhook address (http:// or https://). Empty = does nothing.")
        private String url = "";

        @Comment("On/off switch for this webhook.")
        private boolean enable = false;

        @Setter(AccessLevel.NONE)
        @Comment("false = formatted message (JSON), true = plain text.")
        private boolean cleanText = false;
    }
}