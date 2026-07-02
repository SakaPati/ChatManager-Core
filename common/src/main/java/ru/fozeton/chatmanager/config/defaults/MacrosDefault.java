package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.MacrosConfig;

public class MacrosDefault implements Default<MacrosConfig> {
    @Override
    public MacrosConfig createDefault() {
        MacrosConfig config = new MacrosConfig();

        config.getMacros().put("Hello", new MacrosConfig.Macros(30, false));

        return config;
    }

    @Override
    public boolean isEmpty(MacrosConfig config) {
        return config == null || config.getMacros() == null || config.getMacros().isEmpty();
    }
}
