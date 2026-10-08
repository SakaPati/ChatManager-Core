package ru.fozeton.chatmanager.utils.compat.providers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.exceptions.NotInitializedException;
import ru.fozeton.chatmanager.utils.compat.api.IClickEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClickEventProvider {
    @Setter
    private static IClickEvent instance;

    public static Component applyClickEvent(
            Component content,
            ClickEvent.Action action,
            String value,
            @Nullable String commandPrefix
    ) {
        if (instance == null) {
            throw new NotInitializedException(
                    """
                            IClickEvent has not been initialized!
                            Call ClickEventProvider.setInstance(...) during client initialization.
                            """.stripIndent());
        }
        return instance.applyClickEvent(content, action, value, commandPrefix);
    }
}
