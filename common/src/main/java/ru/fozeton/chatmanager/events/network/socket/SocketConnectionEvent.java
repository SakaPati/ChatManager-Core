package ru.fozeton.chatmanager.events.network.socket;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.http.WebSocket;

@Getter
@RequiredArgsConstructor
public class SocketConnectionEvent extends Event<SocketConnectionEvent> {
    final WebSocket webSocket;
}
