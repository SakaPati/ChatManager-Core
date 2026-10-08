package ru.fozeton.chatmanager.network.sockets;

import com.google.gson.JsonObject;

public record CustomPacket(String type, JsonObject data) implements SocketPacket {
}
