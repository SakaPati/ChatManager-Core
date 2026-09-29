package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.mixin.ScreenInvoker;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;
import ru.fozeton.chatmanager.utils.compat.providers.GraphicsProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Screen26_2 implements IScreen {
    @Getter
    private static final Screen26_2 instance = new Screen26_2();

    @Override
    public Screen getScreen() {
        return Minecraft.getInstance().gui.screen();
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        Screen screen = getScreen();
        if (screen == null || style == null) return false;

        ClickEvent click = style.getClickEvent();
        if (click == null) return false;

        ScreenInvoker.chatmanager$handleGameClickEvent(click, Minecraft.getInstance(), screen);
        return true;
    }

    @Override
    public @Nullable Style getStyleAt(Font font, FormattedCharSequence line, int x) {
        if (x < 0) return null;
        int[] width = {0};
        Style[] result = {null};
        line.accept((index, style, codePoint) -> {
            int w = font.width(FormattedCharSequence.codepoint(codePoint, style));
            if (x < width[0] + w) {
                result[0] = style;
                return false;
            }
            width[0] += w;
            return true;
        });
        return result[0];
    }

    @Override
    public void renderHoverEffect(GraphicsProvider graphics, Font font, Style style, int mouseX, int mouseY) {
        graphics.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }
}
