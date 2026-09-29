package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreenProvider {
    @Setter
    private static IScreen instance;

    private static IScreen require() {
        if (instance == null) {
            throw new IllegalStateException("""
                    IScreen has not been initialized!
                    Please call ScreenProvider.setInstance(...) during client initialization.
                    """.stripIndent());
        }
        return instance;
    }

    public static Screen getScreen() {
        return require().getScreen();
    }

    public static boolean handleComponentClicked(Style style) {
        return require().handleComponentClicked(style);
    }

    public static @Nullable Style getStyleAt(Font font, FormattedCharSequence line, int x) {
        return require().getStyleAt(font, line, x);
    }

    public static void renderHoverEffect(GraphicsProvider graphics, Font font, Style style, int mouseX, int mouseY) {
        require().renderHoverEffect(graphics, font, style, mouseX, mouseY);
    }
}
