package ru.fozeton.chatmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MacrosConfig implements IConfig{
    private boolean enabled = true;
    private final Map<String, Macros> macros = new HashMap<>();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Macros {
        private int time;
        private boolean isActive;
    }
}