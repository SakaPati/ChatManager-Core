package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.api.ILocalPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalPlayerProvider {
    @Setter
    private static ILocalPlayer instance;

    private static ILocalPlayer require() {
        if (instance == null) {
            throw new IllegalStateException(
                    """
                            ILocalPlayer has not been initialized!
                            Please call LocalPlayerProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance;
    }

    public static void displayClientMessage(Component component, boolean overlay) {
        require().displayClientMessage(component, overlay);
    }
}
