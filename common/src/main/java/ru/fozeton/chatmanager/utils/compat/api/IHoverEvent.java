package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public interface IHoverEvent {
    HoverEvent createShowText(Component text);
}
