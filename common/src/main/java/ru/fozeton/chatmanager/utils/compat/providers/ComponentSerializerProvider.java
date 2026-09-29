package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.utils.compat.api.IComponentSerializer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentSerializerProvider {
    @Setter
    private static IComponentSerializer instance;

    public static String toJson(Component component) {
        if (instance == null) {
            throw new IllegalStateException(
                    """
                            IComponentSerializer has not been initialized!
                            Please call ComponentSerializerProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance.toJson(component);
    }
}