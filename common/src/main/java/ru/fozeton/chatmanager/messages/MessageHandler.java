package ru.fozeton.chatmanager.messages;

public interface MessageHandler {
    boolean action(Message message);
}
