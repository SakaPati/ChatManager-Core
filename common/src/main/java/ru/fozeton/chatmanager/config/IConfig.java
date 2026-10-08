package ru.fozeton.chatmanager.config;

import me.shedaniel.autoconfig.ConfigData;

public interface IConfig extends ConfigData {
    default void applyDefaults() {
    }

    static int parseColor(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Long.decode(value.trim()).intValue();
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}