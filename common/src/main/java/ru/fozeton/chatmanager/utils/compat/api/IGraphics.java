package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

public interface IGraphics {
    void renderComponentHoverEffect(Font font, Style style, int mouseX, int mouseY);

}
