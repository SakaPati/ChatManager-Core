package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.LinkedList;
import java.util.List;

import static ru.fozeton.chatmanager.config.IConfig.parseColor;

@Getter
@Setter
@Config(name = "messagesFilter")
public class MessagesFilterConfig implements IConfig {
    @ConfigEntry.Gui.Excluded
    private final List<MessageFilter> filters = new LinkedList<>();

    @Override
    public void applyDefaults() {
        MessagesFilterConfig.MessageFilter plainTextFilter = new MessagesFilterConfig.MessageFilter(
                "test",
                "0xFF55FF55",
                "0xFF55FF55"
        );

        MessagesFilterConfig.MessageFilter regexFilter = new MessagesFilterConfig.MessageFilter(
                "^(Тест|Test)",
                "0xFFFF5555",
                "0xFFFF5555"
        );

        MessagesFilterConfig.MessageFilter advancedFilter = new MessagesFilterConfig.MessageFilter(
                "Ⓖ Fozeton\\[\\d+\\]: .*",
                "0xFF7C6EF5",
                "0xFF7C6EF5"
        );

        filters.add(plainTextFilter);
        filters.add(regexFilter);
        filters.add(advancedFilter);
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class MessageFilter {
        private final String pattern;
        private final String borderColor;
        private final String lineColor;
        private String replyMessage;

        public int getBorderColor() {
            return parseColor(borderColor, 0xFFFFFFFF);
        }

        public int getLineColor() {
            return parseColor(lineColor, 0xFFFFFFFF);
        }
    }
}