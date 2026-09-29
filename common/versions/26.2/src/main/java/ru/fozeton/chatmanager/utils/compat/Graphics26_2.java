package ru.fozeton.chatmanager.utils.compat;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStackTemplate;
import ru.fozeton.chatmanager.utils.compat.api.IGraphics;

@RequiredArgsConstructor
public class Graphics26_2 implements IGraphics {
    private final GuiGraphicsExtractor guiGraphics;

    @Override
    public void renderComponentHoverEffect(Font font, Style style, int mouseX, int mouseY) {
        HoverEvent hover = style.getHoverEvent();
        if (hover instanceof HoverEvent.ShowText(Component value)) {
            guiGraphics.setTooltipForNextFrame(
                    font,
                    font.split(value, Math.max(guiGraphics.guiWidth() / 2, 200)),
                    mouseX, mouseY
            );
        } else if (hover instanceof HoverEvent.ShowItem(ItemStackTemplate item)) {
            guiGraphics.setTooltipForNextFrame(font, item.create(), mouseX, mouseY);
        }
    }
}
