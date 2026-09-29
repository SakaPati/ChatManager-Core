package ru.fozeton.chatmanager.utils.compat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.utils.compat.api.IClickEvent;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClickEvent26_2 implements IClickEvent {
    @Getter
    private static final ClickEvent26_2 instance = new ClickEvent26_2();

    @Override
    public Component applyClickEvent(
            Component content,
            ClickEvent.Action action,
            String value,
            @Nullable String commandPrefix
    ) {
        ClickEvent clickEvent = createClickEvent(action, value);

        if (commandPrefix != null) {
            MutableComponent replaced = tryReplace(content, commandPrefix, clickEvent);
            if (replaced != null) {
                return replaced;
            }
        }

        Style newStyle = content.getStyle().withClickEvent(clickEvent);
        return content.copy().setStyle(newStyle);
    }

    private ClickEvent createClickEvent(ClickEvent.Action action, String value) {
        return switch (action) {
            case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(value);
            case OPEN_URL -> new ClickEvent.OpenUrl(URI.create(value));
            case OPEN_FILE -> new ClickEvent.OpenFile(value);
            case CUSTOM -> new ClickEvent.Custom(
                    Identifier.fromNamespaceAndPath("chatmanager", "click"),
                    Optional.of(StringTag.valueOf(value))
            );
            case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(value);
            case CHANGE_PAGE -> new ClickEvent.ChangePage(Integer.parseInt(value));
            default -> new ClickEvent.RunCommand(value);
        };
    }

    private @Nullable MutableComponent tryReplace(Component node, String commandPrefix, ClickEvent newEvent) {
        ClickEvent existing = node.getStyle().getClickEvent();

        if (existing instanceof ClickEvent.Custom custom && custom.id().getPath().startsWith(commandPrefix)) {
            Style newStyle = node.getStyle().withClickEvent(newEvent);
            return node.copy().setStyle(newStyle);
        }

        List<Component> siblings = node.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            MutableComponent replacedChild = tryReplace(siblings.get(i), commandPrefix, newEvent);
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