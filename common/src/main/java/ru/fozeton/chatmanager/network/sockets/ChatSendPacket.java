package ru.fozeton.chatmanager.network.sockets;

public record ChatSendPacket(String text) implements SocketPacket {
}
