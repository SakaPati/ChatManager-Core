package ru.fozeton.chatmanager.events;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import ru.fozeton.chatmanager.config.ChannelsConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.messages.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class MessageReceivedEvent extends Event<MessageReceivedEvent> {
    final Message message;
    private final ChannelsConfig channelsConfig = ChatConfigManager.getInstance().load(ChannelsConfig.class);

    public MessageReceivedEvent(Message message) {
        if (!channelsConfig.isUseTimeFormatter()) {
            this.message = message;
            return;
        }

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fullDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(new Date());
        int color = Long.decode(ChatConfigManager.getInstance().load(ChannelsConfig.class).getTimeColor()).intValue();

        Component timePart = Component.literal(time + " ")
                .withColor(color)
                .withStyle(style -> style.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(fullDate))
                ));

        message.setContent(Component.empty().append(timePart).append(message.getContent()));
        this.message = message;
    }
}