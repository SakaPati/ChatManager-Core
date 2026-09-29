package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.utils.compat.api.IClickEvent;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClickEvent1_21_4 implements IClickEvent {
    @Getter
    private static final ClickEvent1_21_4 instance = new ClickEvent1_21_4();

    @Override
    public Component applyClickEvent(
            Component content,
            ClickEvent.Action action,
            String value,
            @Nullable String commandPrefix
    ) {
        if (commandPrefix != null) {
            MutableComponent replaced = tryReplace(content, commandPrefix, action, value);
            if (replaced != null) {
                return replaced;
            }
        }

        ClickEvent clickEvent = content.getStyle().getClickEvent();
        String newValue = clickEvent == null ? value : value + " \"" + clickEvent.getValue() + "\"";
        Style newStyle = content.getStyle().withClickEvent(new ClickEvent(action, newValue));
        return content.copy().setStyle(newStyle);
    }

    private @Nullable MutableComponent tryReplace(
            Component node,
            String commandPrefix,
            ClickEvent.Action action,
            String value
    ) {
        ClickEvent existing = node.getStyle().getClickEvent();

        if (existing != null && existing.getValue().startsWith(commandPrefix)) {
            String newValue = value + " \"" + existing.getValue() + "\"";
            Style newStyle = node.getStyle().withClickEvent(new ClickEvent(action, newValue));
            return node.copy().setStyle(newStyle);
        }

        List<Component> siblings = node.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            MutableComponent replacedChild = tryReplace(siblings.get(i), commandPrefix, action, value);
            if (replacedChild != null) {
                MutableComponent rebuilt = node.plainCopy().setStyle(node.getStyle());
                for (int j = 0; j < siblings.size(); j++) {
                    rebuilt.append(j == i ? replacedChild : siblings.get(j));
                }
                return rebuilt;
            }
        }
        return null;
    }
}