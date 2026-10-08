package ru.fozeton.chatmanager.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.NetworkConfig;
import ru.fozeton.chatmanager.events.network.socket.SocketCloseEvent;
import ru.fozeton.chatmanager.events.network.socket.SocketConnectionEvent;
import ru.fozeton.chatmanager.events.network.socket.SocketErrorEvent;
import ru.fozeton.chatmanager.events.network.socket.SocketPacketReceivedEvent;
import ru.fozeton.chatmanager.network.sockets.*;
import ru.fozeton.chatmanager.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the WebSocket connection to the backend configured in {@link ru.fozeton.chatmanager.config.NetworkConfig#getSocketUrl()}.
 * <p>
 * Handles connecting, automatic reconnection with exponential backoff (up to 5 attempts),
 * and decoding of incoming text frames into {@link SocketPacket}s, which are then published
 * on the event bus as {@link SocketPacketReceivedEvent}.
 * <p>
 * Callbacks of this class run on the HTTP client's worker threads, not on the Minecraft main thread.
 */
public class WebSocketHandler {
    private final HttpClient client = NetworkManager.getInstance().getClient();
    private final NetworkConfig networkConfig = ChatConfigManager.getInstance().getNetworkConfig();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            Thread.ofVirtual().factory()
    );
    private final Logger log = new Logger(WebSocketHandler.class);
    private final Gson gson = new Gson();
    private final AtomicBoolean isReconnection = new AtomicBoolean(false);
    @Getter
    private WebSocket ws;
    private int connectionCounter = 0;

    /**
     * Opens a WebSocket connection to the configured URL.
     * Does nothing if the URL is blank. On handshake failure, schedules a reconnect.
     */
    public void connection() {
        String url = networkConfig.getSocketUrl();
        if (url.isBlank()) return;

        log.info(String.format("Initiating WebSocket connection to URL: %s (Attempt #%d)", url, connectionCounter + 1));

        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new SocketListener())
                .thenAccept(webSocket -> {
                    ws = webSocket;
                    connectionCounter = 0;
                    log.info(String.format("WebSocket connection successfully established to: %s", url));
                })
                .exceptionally(ex -> {
                    log.error(
                            String.format(
                                    "Failed to establish WebSocket handshake with URL: %s. Error: %s",
                                    url,
                                    ex.getMessage()
                            ), ex
                    );
                    reconnect();
                    return null;
                });
    }

    /**
     * Schedules a reconnection attempt with exponential backoff ({@code 2^attempt} seconds).
     * Gives up after 5 failed attempts. Does nothing if a reconnect is already scheduled.
     */
    public void reconnect() {
        if (connectionCounter == 5) {
            log.error("Could not reconnect to the WebSocket server after 5 attempts. Giving up.");
            connectionCounter = 0;
            return;
        }

        if (isReconnection.get()) return;
        isReconnection.set(true);

        connectionCounter++;
        int delay = (int) Math.pow(2, connectionCounter);

        log.warn(String.format(
                "Reconnection attempt #%d in %d seconds due to connection failure.",
                connectionCounter,
                delay
        ));

        scheduler.schedule(
                () -> {
                    try {
                        isReconnection.set(false);
                        connection();
                    } catch (Exception e) {
                        log.error("Unexpected error during executing reconnection task", e);
                        isReconnection.set(false);
                    }
                }, delay, TimeUnit.SECONDS
        );
    }


    /**
     * Wire format of a message received from the backend.
     * <p>
     * Example: {@code {"type": "COMMAND", "id": "<uuid>", "data": {"command": "time set day"}}}
     */
    @Getter
    @RequiredArgsConstructor
    public static class SocketMessage {
        private final Type type;
        private final UUID id;
        private final JsonObject data;

        /** Kind of packet, determines how {@code data} is decoded. */
        public enum Type {
            /** Adds a local message to the chat (visible only to this player). */
            ADD,
            /** Sends a chat message as the player. */
            SEND,
            /** Executes a command as the player (must be in the allowed commands list). */
            COMMAND,
            /** Application-specific packet with a custom payload. */
            CUSTOM
        }
    }

    /**
     * Listener for WebSocket events. Reassembles fragmented text frames, parses them into
     * {@link SocketMessage}s and publishes the resulting packets on the event bus.
     */
    public class SocketListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        /** Publishes a {@link SocketConnectionEvent} when the connection is opened. */
        @Override
        public void onOpen(WebSocket webSocket) {
            log.info(String.format(
                    "WebSocket connection successfully opened to URL: %s",
                    networkConfig.getSocketUrl()
            ));
            ChatManagerCore.EVENT_BUS.activate(new SocketConnectionEvent(webSocket));
            WebSocket.Listener.super.onOpen(webSocket);
        }

        /**
         * Accumulates fragments of a text frame and, once the frame is complete, decodes it into a
         * {@link SocketPacket} and publishes a {@link SocketPacketReceivedEvent}.
         * Frames larger than 1,000,000 characters are rejected and the connection is closed with code 1009.
         * Malformed JSON is logged and ignored.
         */
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if(!last) return WebSocket.Listener.super.onText(webSocket, data, last);
            if (buffer.length() > 1_000_000) {
                buffer.setLength(0);
                webSocket.sendClose(1009, "Message too big");
                return null;
            }

            String rawJson = buffer.toString();
            buffer.setLength(0);

            try {
                SocketMessage msg = gson.fromJson(rawJson, SocketMessage.class);
                if (msg == null) {
                    log.warn("Received empty or invalid WebSocket text frame.");
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }

                SocketMessage.Type type = msg.getType();
                UUID id = msg.getId();
                JsonObject pktData = msg.getData();

                log.info(String.format("Received packet [Type: %s, ID: %s]", type, id));
                log.debug(String.format("Packet payload: %s", pktData));

                SocketPacket pkt = switch (type) {
                    case ADD -> gson.fromJson(pktData, ChatAddPacket.class);
                    case SEND -> gson.fromJson(pktData, ChatSendPacket.class);
                    case COMMAND -> gson.fromJson(pktData, ChatCommandPacket.class);
                    default -> gson.fromJson(pktData, CustomPacket.class);
                };

                ChatManagerCore.EVENT_BUS.activate(new SocketPacketReceivedEvent(id, pkt, webSocket));
            } catch (JsonSyntaxException e) {
                log.error(String.format("Failed to parse incoming JSON frame. Raw data: %s", rawJson), e);
            } catch (Exception e) {
                log.error("Unexpected error occurred while handling processing onText frame", e);
            }

            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        /** Publishes a {@link SocketCloseEvent} and schedules a reconnect. */
        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            String cleanReason = (reason == null || reason.isBlank()) ? "No reason provided" : reason;
            log.warn(String.format(
                    "WebSocket connection closed by remote peer. Status code: %d | Reason: %s",
                    statusCode,
                    cleanReason
            ));
            ChatManagerCore.EVENT_BUS.activate(new SocketCloseEvent(webSocket, statusCode, reason));
            reconnect();
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        /** Publishes a {@link SocketErrorEvent} and schedules a reconnect. */
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error(String.format("WebSocket transport layer error occurred: %s", error.getMessage()), error);
            ChatManagerCore.EVENT_BUS.activate(new SocketErrorEvent(webSocket, error));
            reconnect();
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}