package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.MathConfig;

public class MathDefault implements Default<MathConfig> {
    @Override
    public MathConfig createDefault() {
        MathConfig config = new MathConfig();
        config.getFunctions().add("area(w;h)=w*h");
        return config;
    }

    @Override
    public boolean isEmpty(MathConfig config) {
        return config == null;
    }
}