package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.compat.api.IGameProfile;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameProfileProvider {
    @Setter
    private static IGameProfile instance;

    private static IGameProfile require() {
        if (instance == null) {
            throw new NotInitializedException("""
                    IGameProfile has not been initialized!
                    Please call GameProfileProvider.setInstance(...) during client initialization.
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
