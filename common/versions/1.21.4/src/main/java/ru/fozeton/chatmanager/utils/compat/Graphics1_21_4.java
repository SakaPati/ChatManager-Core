package ru.fozeton.chatmanager.utils.compat;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import ru.fozeton.chatmanager.utils.compat.api.IGraphics;

@RequiredArgsConstructor
public class Graphics1_21_4 implements IGraphics {
    private final GuiGraphics guiGraphics;

    @Override
    public void renderComponentHoverEffect(Font font, Style style, int mouseX, int mouseY) {
        this.guiGraphics.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }
}
