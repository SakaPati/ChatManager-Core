package ru.fozeton.chatmanager.channel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.channel.ChannelAddedEvent;
import ru.fozeton.chatmanager.events.channel.MessageAddedToChannelEvent;
import ru.fozeton.chatmanager.events.channel.MessageStackEvent;
import ru.fozeton.chatmanager.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
public class ChatChannel {
    @Getter
    private static final List<Message> messageHistory = Collections.synchronizedList(new ArrayList<>());
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
    private String id;
    private String name;
    private boolean visible = true;
    private int maxHistoryMessage = 100;
    private int maxVisibleMessage = 10;
    private Map<String, Object> metadata;

    public ChatChannel(String id, String name) {
        this.id = id;
        this.name = name;
        ChatManagerCore.EVENT_BUS.activate(new ChannelAddedEvent(this));
        ChatManagerCore.registerChannel(this.id, this);
    }

    public void addMessage(Message message) {
        synchronized (messages) {
            long segment = System.currentTimeMillis() - 5000;
            for (Message msg : messages) {
                if (msg.getTimestamp().toEpochMilli() < segment) break;

                if (msg.getPlainText().equals(message.getPlainText())) {
                    msg.setStack(msg.getStack() + 1);
                    ChatManagerCore.EVENT_BUS.activate(new MessageStackEvent(msg.getId(), msg, this));

                    return;
                }
            }

            if (messages.size() >= maxHistoryMessage) messages.removeLast();
            messages.addFirst(message);
            messageHistory.addFirst(message);
            ChatManagerCore.EVENT_BUS.activate(new MessageAddedToChannelEvent(message, this));
        }
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        this.messages.clear();
    }

    public void forEachMessage(Consumer<List<Message>> action) {
        synchronized (messages) {
            action.accept(messages);
        }
    }
}
