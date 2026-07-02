package ru.fozeton.chatmanager.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class AliasConfig implements IConfig{
    private boolean enabled = true;
    private boolean applyToCommands = true;
    private List<Alias> aliases = new LinkedList<>();

    @Getter
    @AllArgsConstructor
    public static class Alias {
        private String key;
        private String value;
    }
}