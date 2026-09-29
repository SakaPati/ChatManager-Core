package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.screens.Screen;
import ru.fozeton.chatmanager.utils.compat.api.IScreen;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreenProvider {
    @Setter
    private static IScreen instance;

    public static Screen getScreen() {
        if (instance == null) {
            throw new IllegalStateException(
                    """
                            IScreen has not been initialized!
                            Please call ScreenProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance.getScreen();
    }
}
