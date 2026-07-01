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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private final ChannelsConfig channelsConfig = ChatConfigManager.getInstance().load(ChannelsConfig.class);

    public Message(String id, @Nullable String author, Component content, String plainText, MessageType type, Instant timestamp) {
        this.id = id;
        this.author = author;
        this.originalContent = useTimeFormatter(content);
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
        ClickEvent clickEvent = style.getClickEvent();
        String newValue = clickEvent == null ? value : value + " \"" + clickEvent.getValue() + "\"";
        this.style = style.withClickEvent(new ClickEvent(action, newValue));
        this.content = content.copy().setStyle(this.style);
    }

    public MutableComponent getMutContent() {
        return this.originalContent.copy();
    }

    protected Component useTimeFormatter(Component content) {
        if (!channelsConfig.isUseTimeFormatter()) return content;

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fullDate = new SimpleDateFormat("HH:mm:ss.SSS dd.MM.yyyy").format(new Date());
        int color = Long.decode(ChatConfigManager.getInstance().load(ChannelsConfig.class).getTimeColor()).intValue();

        Component timePart = Component.literal(time + " ")
                .withColor(color)
                .withStyle(style -> style.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(fullDate))
                ));

        return Component.empty().append(timePart).append(content);
    }
}