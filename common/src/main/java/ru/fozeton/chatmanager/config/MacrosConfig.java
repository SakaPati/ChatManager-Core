package ru.fozeton.chatmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Config(name = "macros")
public class MacrosConfig implements IConfig {
    @ConfigEntry.Gui.Excluded
    private final Map<String, Macros> macros = new HashMap<>();
    private boolean enabled = true;

    @Override
    public void applyDefaults() {
        macros.put("Hello", new MacrosConfig.Macros(30, false));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Macros {
        private int time;
        private boolean isActive;
    }
}