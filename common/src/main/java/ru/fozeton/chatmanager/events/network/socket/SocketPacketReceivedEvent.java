package ru.fozeton.chatmanager.events.network.socket;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.network.sockets.SocketPacket;

import java.net.http.WebSocket;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SocketPacketReceivedEvent extends Event<SocketPacketReceivedEvent> {
    final UUID id;
    final SocketPacket packet;
    final WebSocket webSocket;
}
