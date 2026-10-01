package ru.fozeton.chatmanager.utils.compat.api;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.utils.compat.providers.GraphicsProvider;

public interface IScreen {
    Screen getScreen();

    boolean handleComponentClicked(Style style);

    @Nullable Style getStyleAt(Font font, FormattedCharSequence line, int x);

    void renderHoverEffect(GraphicsProvider graphics, Font font, Style style, int mouseX, int mouseY);

    default boolean isChatScreen() {
        return getScreen() instanceof ChatScreen;
    }
}
