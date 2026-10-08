package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.compat.api.IHoverEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HoverEventProvider {
    @Setter
    private static IHoverEvent instance;

    public static HoverEvent showText(Component text) {
        if (instance == null) {
            throw new NotInitializedException(
                    """
                            IHoverEvent has not been initialized!
                            Please call HoverEventProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance.createShowText(text);
    }
}
