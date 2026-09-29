package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.network.chat.Component;

public interface IComponentSerializer {
    String toJson(Component component);
}
