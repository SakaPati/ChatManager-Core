package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import ru.fozeton.chatmanager.utils.compat.api.IGraphics;

@RequiredArgsConstructor
public class GraphicsProvider implements IGraphics {
    private final IGraphics implementation;

    public void renderComponentHoverEffect(Font font, Style style, int mouseX, int mouseY) {
        this.implementation.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }
}
