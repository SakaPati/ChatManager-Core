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

    public void reconnect() {
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


    @Getter
    @RequiredArgsConstructor
    public static class SocketMessage {
        private final Type type;
        private final UUID id;
        private final JsonObject data;

        public enum Type {
            ADD,
            SEND,
            COMMAND,
            CUSTOM
        }
    }

    public class SocketListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            log.info(String.format(
                    "WebSocket connection successfully opened to URL: %s",
                    networkConfig.getSocketUrl()
            ));
            ChatManagerCore.EVENT_BUS.activate(new SocketConnectionEvent(webSocket));
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String rawJson = data.toString();

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
                    case CUSTOM -> gson.fromJson(pktData, CustomPacket.class);
                };

                ChatManagerCore.EVENT_BUS.activate(new SocketPacketReceivedEvent(id, pkt, webSocket));
            } catch (JsonSyntaxException e) {
                log.error(String.format("Failed to parse incoming JSON frame. Raw data: %s", rawJson), e);
            } catch (Exception e) {
                log.error("Unexpected error occurred while handling processing onText frame", e);
            }

            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

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

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error(String.format("WebSocket transport layer error occurred: %s", error.getMessage()), error);
            ChatManagerCore.EVENT_BUS.activate(new SocketErrorEvent(webSocket, error));
            reconnect();
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}
