package ru.fozeton.chatmanager.events.network.socket;

import com.ferra13671.megaevents.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.http.WebSocket;

@Getter
@RequiredArgsConstructor
public class SocketErrorEvent extends Event<SocketErrorEvent> {
    final WebSocket webSocket;
    final Throwable error;
}
