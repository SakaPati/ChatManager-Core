package ru.fozeton.chatmanager.network.sockets;

public record ChatCommandPacket(String command) implements SocketPacket {
}
