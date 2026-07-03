package ru.fozeton.chatmanager.messages;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
public class Message {
    private final String id;
    @Nullable
    private final String author;
    private final String plainText;
    private final Component originalContent;
    private final Instant timestamp;
    private MessageType type;
    private Component content;
    private Style style;
    private int stack = 1;
    private Map<String, Object> metadata = new HashMap<>();

    public Message(String id, @Nullable String author, Component content, String plainText, MessageType type, Instant timestamp) {
        this.id = id;
        this.author = author;
        this.originalContent = content;
        this.content = this.originalContent;
        this.style = content.getStyle();
        this.plainText = plainText;
        this.type = type;
        this.timestamp = timestamp;
    }

    @Nullable
    public ChatChannel getChannel() {
        return (ChatChannel) metadata.get("channel");
    }

    public void setChannel(ChatChannel channel) {
        metadata.put("channel", channel);
    }

    public void setClickEvent(ClickEvent.Action action, String value) {
        setClickEvent(action, value, null);
    }

    public void setClickEvent(ClickEvent.Action action, String value, @Nullable String commandPrefix) {
        if (commandPrefix != null) {
            MutableComponent replaced = tryReplace(this.content, commandPrefix, action, value);
            if (replaced != null) {
                this.content = replaced;
                this.style = replaced.getStyle();
                return;
            }
        }

        ClickEvent clickEvent = style.getClickEvent();
        String newValue = clickEvent == null ? value : value + " \"" + clickEvent.getValue() + "\"";
        this.style = style.withClickEvent(new ClickEvent(action, newValue));
        this.content = content.copy().setStyle(this.style);
    }

    @Nullable
    private MutableComponent tryReplace(Component node, String commandPrefix, ClickEvent.Action action, String value) {
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
                MutableComponent rebuilt = node.copy();
                List<Component> newSiblings = new ArrayList<>(rebuilt.getSiblings());
                newSiblings.set(i, replacedChild);
                rebuilt.getSiblings().clear();
                rebuilt.getSiblings().addAll(newSiblings);
                return rebuilt;
            }
        }
        return null;
    }

    public MutableComponent getMutContent() {
        return this.originalContent.copy();
    }
}