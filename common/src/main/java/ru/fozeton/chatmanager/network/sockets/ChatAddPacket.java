package ru.fozeton.chatmanager.network.sockets;

import org.jetbrains.annotations.Nullable;

public record ChatAddPacket(String text, @Nullable String componentJson, boolean overlay) implements SocketPacket {
}
