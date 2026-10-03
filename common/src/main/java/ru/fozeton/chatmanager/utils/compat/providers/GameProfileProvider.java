package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fozeton.chatmanager.utils.compat.api.IGameProfile;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameProfileProvider {
    @Setter
    private static IGameProfile instance;

    private static IGameProfile require() {
        if (instance == null) {
            throw new IllegalStateException("""
                    IGameProfile has not been initialized!
                    Please call ScreenProvider.setInstance(...) during client initialization.
                    """.stripIndent());
        }
        return instance;
    }

    public static String getName() {
        return require().name();
    }

    public static UUID getId() {
        return require().id();
    }
}
