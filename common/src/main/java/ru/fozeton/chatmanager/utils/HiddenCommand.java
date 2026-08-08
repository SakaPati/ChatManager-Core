package ru.fozeton.chatmanager.utils;

import org.jetbrains.annotations.Nullable;

public enum HiddenCommand {
    LOGIN("/login ", "/l "),
    REGISTER("/register ", "/reg "),
    CHANGE_PASSWORD("/changepassword ", "/changepass ");

    private final String prefix;
    @Nullable
    private final String[] aliases;

    HiddenCommand(String prefix) {
        this.prefix = prefix;
        this.aliases = null;
    }

    HiddenCommand(String prefix, String... aliases) {
        this.prefix = prefix;
        this.aliases = aliases;
    }

    public static boolean isHidden(String text) {
        for (HiddenCommand cmd : values()) {
            if (text.startsWith(cmd.prefix)) return true;

            String[] aliases = cmd.aliases;
            if (aliases != null) {
                for (String alias : aliases) {
                    if (text.startsWith(alias)) return true;
                }
            }
        }
        return false;
    }
}