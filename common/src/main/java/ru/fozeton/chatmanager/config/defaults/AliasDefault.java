package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.AliasConfig;

import java.util.List;

public class AliasDefault implements Default<AliasConfig> {
    @Override
    public AliasConfig createDefault() {
        AliasConfig config = new AliasConfig();
        List<AliasConfig.Alias> aliases = config.getAliases();

        aliases.add(new AliasConfig.Alias("!lvl", "level"));
        aliases.add(new AliasConfig.Alias("!cb", "count of blocks"));
        aliases.add(new AliasConfig.Alias("!don", "donate"));
        aliases.add(new AliasConfig.Alias("!tp", "teleport"));

        return config;
    }

    @Override
    public boolean isEmpty(AliasConfig config) {
        return config == null || config.getAliases() == null || config.getAliases().isEmpty();
    }
}
