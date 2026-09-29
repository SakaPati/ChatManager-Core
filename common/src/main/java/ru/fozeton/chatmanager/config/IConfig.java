package ru.fozeton.chatmanager.config;

import me.shedaniel.autoconfig.ConfigData;

public interface IConfig extends ConfigData {
    default void applyDefaults() {
    }
}