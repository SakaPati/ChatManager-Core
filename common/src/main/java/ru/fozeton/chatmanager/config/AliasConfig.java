package ru.fozeton.chatmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Config(name = "alias")
public class AliasConfig implements IConfig {
    private boolean enabled = true;
    private boolean applyToCommands = true;
    @ConfigEntry.Gui.Excluded
    private List<Alias> aliases = new LinkedList<>();

    @Override
    public void applyDefaults() {
        aliases.add(new Alias("!lvl", "level"));
        aliases.add(new Alias("!cb", "count of blocks"));
        aliases.add(new Alias("!don", "donate"));
        aliases.add(new Alias("!tp", "teleport"));
    }

    @Getter
    @AllArgsConstructor
    public static class Alias {
        private String key;
        private String value;
    }
}