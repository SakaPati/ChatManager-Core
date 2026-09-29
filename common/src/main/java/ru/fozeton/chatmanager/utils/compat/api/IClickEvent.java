package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IClickEvent {
    Component applyClickEvent(Component content, ClickEvent.Action action, String value, @Nullable String commandPrefix);
}
