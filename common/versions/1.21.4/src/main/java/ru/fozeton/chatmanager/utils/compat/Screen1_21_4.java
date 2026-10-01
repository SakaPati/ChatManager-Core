package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;
import ru.fozeton.chatmanager.utils.compat.providers.GraphicsProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Screen1_21_4 implements IScreen {
    @Getter
    private static final Screen1_21_4 instance = new Screen1_21_4();

    @Override
    public Screen getScreen() {
        return Minecraft.getInstance().screen;
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        if (getScreen() == null) return false;
        return getScreen().handleComponentClicked(style);
    }

    @Override
    public @Nullable Style getStyleAt(Font font, FormattedCharSequence line, int x) {
        return font.getSplitter().componentStyleAtWidth(line, x);
    }

    @Override
    public void renderHoverEffect(GraphicsProvider graphics, Font font, Style style, int mouseX, int mouseY) {
        graphics.renderComponentHoverEffect(font, style, mouseX, mouseY);
    }
}
