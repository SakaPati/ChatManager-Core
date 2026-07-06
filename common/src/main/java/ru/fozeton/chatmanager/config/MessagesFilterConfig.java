package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class MessagesFilterConfig implements IConfig{
    private final List<MessageFilter> filters = new LinkedList<>();

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class MessageFilter {
        private final String pattern;
        private final String borderColor;
        private final String lineColor;
        private String replyMessage;
    }
}