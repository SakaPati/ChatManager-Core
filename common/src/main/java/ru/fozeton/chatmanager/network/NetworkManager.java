package ru.fozeton.chatmanager.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.messages.Message;

import java.net.http.HttpClient;

/**
 * Singleton entry point for network features: webhooks and the WebSocket connection.
 * Owns the shared {@link HttpClient} used by both handlers.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkManager {
    @Getter
    private static final NetworkManager instance = new NetworkManager();
    private final HttpClient client = HttpClient.newHttpClient();

    @Setter
    @Nullable
    private WebHooksHandler webHooksHandler;

    @Nullable
    private WebSocketHandler webSocketHandler;

    /**
     * Forwards a chat message to the webhook handler, if one is set.
     * @param message the message to dispatch
     */
    public void dispatcher(Message message) {
        if (webHooksHandler != null) webHooksHandler.onWebHook(message);
    }

    /** Connects to the WebSocket backend, if a socket handler is set. */
    public void connectSocket() {
        if (webSocketHandler != null) webSocketHandler.connection();
    }

    /**
     * Sets the WebSocket handler.
     * @param socketHandler the handler to use
     * @return the same handler, for chaining
     */
    public WebSocketHandler setWebSocketHandler(WebSocketHandler socketHandler) {
        this.webSocketHandler = socketHandler;
        return this.webSocketHandler;
    }
}
