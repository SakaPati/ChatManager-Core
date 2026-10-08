package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.compat.api.IComponentSerializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentSerializerProvider {
    @Setter
    private static IComponentSerializer instance;

    public static IComponentSerializer require() {
        if (instance == null) {
            throw new NotInitializedException(
                    """
                            IComponentSerializer has not been initialized!
                            Please call ComponentSerializerProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance;
    }

    public static String toJson(Component component) {
        return require().toJson(component);
    }

    public static Component fromJson(String json) {
        return require().fromJson(json);
    }
}