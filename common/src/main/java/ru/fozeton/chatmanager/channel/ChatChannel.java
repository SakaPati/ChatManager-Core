package ru.fozeton.chatmanager.channel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.channel.ChannelAddedEvent;
import ru.fozeton.chatmanager.events.channel.MessageAddedToChannelEvent;
import ru.fozeton.chatmanager.events.channel.MessageStackEvent;
import ru.fozeton.chatmanager.messages.Message;
import ru.fozeton.chatmanager.messages.metadata.Metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A chat channel that stores its own message history.
 * <p>
 * Messages are kept newest first. Identical messages that arrive within 5 seconds of each other
 * are not duplicated, their stack counter is increased instead. All operations on the message list
 * are thread-safe.
 * <p>
 * Creating a channel publishes a {@link ChannelAddedEvent} and registers it in {@link ChatManagerCore}.
 */
@Getter
@Setter
public class ChatChannel {
    /** Messages of all channels in one list, newest first. Shared between all channels. */
    @Getter
    private static final List<Message> messageHistory = Collections.synchronizedList(new ArrayList<>());

    /** Messages of this channel, newest first. */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    /** Unique channel id, used as the key in configs and webhooks. */
    private String id;
    /** Display name of the channel. */
    private String name;
    /** Whether the channel is shown to the player. */
    private boolean visible = true;
    /** Maximum number of messages kept in this channel. Older ones are dropped. */
    private int maxHistoryMessage = 100;
    /** Maximum number of messages shown on screen at once. */
    private int maxVisibleMessage = 10;
    /** Extra data attached to the channel. */
    private Metadata metadata = new Metadata();

    /**
     * Creates a channel, publishes {@link ChannelAddedEvent} and registers it by its id.
     *
     * @param id   unique channel id
     * @param name display name
     */
    public ChatChannel(String id, String name) {
        this.id = id;
        this.name = name;
        ChatManagerCore.EVENT_BUS.activate(new ChannelAddedEvent(this));
        ChatManagerCore.registerChannel(this.id, this);
    }

    /**
     * Adds a message to the channel.
     * <p>
     * If a message with the same plain text was received within the last 5 seconds, no new message
     * is added. Instead, the existing one has its stack counter increased and a {@link MessageStackEvent}
     * is published. Otherwise the message is added to the channel and the global history, the oldest message
     * is removed if {@link #maxHistoryMessage} is exceeded, and a {@link MessageAddedToChannelEvent} is published.
     *
     * @param message the message to add
     */
    public void addMessage(Message message) {
        synchronized (messages) {
            long segment = System.currentTimeMillis() - 5000;
            for (Message msg : messages) {
                if (msg.getTimestamp().toEpochMilli() < segment) break;

                if (msg.getFullPlain().equals(message.getFullPlain())) {
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

    /**
     * Returns a read-only view of the messages, newest first.
     * <p>
     * The view is live. To iterate it safely while other threads add messages,
     * use {@link #withMessages(Function)} or {@link #forEachMessage(Consumer)}.
     *
     * @return unmodifiable view of the channel messages
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /** Removes all messages from this channel. The global {@link #messageHistory} is not affected. */
    public void clear() {
        this.messages.clear();
    }

    /**
     * Runs a function on the message list while holding the lock, and returns its result.
     *
     * @param action function that receives the (modifiable) message list
     * @param <R>    result type
     * @return the value returned by {@code action}
     */
    public <R> R withMessages(Function<List<Message>, R> action) {
        synchronized (messages) {
            return action.apply(messages);
        }
    }

    /**
     * Runs an action on the message list while holding the lock.
     *
     * @param action action that receives the (modifiable) message list
     */
    public void forEachMessage(Consumer<List<Message>> action) {
        synchronized (messages) {
            action.accept(messages);
        }
    }
}