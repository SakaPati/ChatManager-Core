package ru.fozeton.chatmanager.utils.compat.handlers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.messages.Message;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddedMessageHandler {
    public static void handleMessage(Component message){
        Message msg = ChatManagerCore.getMessageParser().parseAddedMessageLocalChat(message);
        ChatManagerCore.EVENT_BUS.activate(new MessageReceivedEvent(msg));
    }
}
