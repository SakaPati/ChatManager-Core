package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.MessagesFilterConfig;

public class MessagesFilterDefault implements Default<MessagesFilterConfig> {
    @Override
    public MessagesFilterConfig createDefault() {
        MessagesFilterConfig config = new MessagesFilterConfig();

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

        config.getFilters().add(plainTextFilter);
        config.getFilters().add(regexFilter);
        config.getFilters().add(advancedFilter);

        return config;
    }

    @Override
    public boolean isEmpty(MessagesFilterConfig config) {
        return config == null || config.getFilters() == null || config.getFilters().isEmpty();
    }
}