package ru.fozeton.chatmanager.mixin.chat;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.AiStyleTextConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.events.InputEvent;
import ru.fozeton.chatmanager.events.game.StylizeMessageEvent;
import ru.fozeton.chatmanager.module.ChatQueueManager;
import ru.fozeton.chatmanager.network.NetworkManager;
import ru.fozeton.chatmanager.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    private final AiStyleTextConfig chatmanager_core$aiStyle = ChatConfigManager.getInstance().getTextStyleConfig();
    @Unique
    private final Gson chatmanager_core$gson = new Gson();
    @Unique
    private final HttpClient chatmanager_core$client = NetworkManager.getInstance().getClient();
    @Unique
    private final Logger chatmanager_core$log = new Logger(ChatScreenMixin.class);
    @Unique
    private final Deque<String> chatmanager_core$undoStack = new ArrayDeque<>();
    @Unique
    private final Deque<String> chatmanager_core$redoStack = new ArrayDeque<>();

    @Shadow
    protected EditBox input;
    @Unique
    private long chatmanager_core$lastKeyPressTime = -1;
    @Unique
    private boolean chatmanager_core$isOperation = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void chatmanager_core$registerBus(CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.register(this);
        input.setMaxLength(1024);
        chatmanager_core$undoStack.clear();
        chatmanager_core$redoStack.clear();
        chatmanager_core$undoStack.add("");
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void chatmanager_core$unregisterBus(CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.unregister(this);

        chatmanager_core$undoStack.clear();
        chatmanager_core$redoStack.clear();
    }

    @Inject(method = "onEdited", at = @At(value = "HEAD"))
    private void onAddUndoStack(String msg, CallbackInfo ci) {
        if (chatmanager_core$isOperation || msg.equals(chatmanager_core$undoStack.peek())) return;

        if ((System.currentTimeMillis() - chatmanager_core$lastKeyPressTime) > 2000) {
            chatmanager_core$lastKeyPressTime = System.currentTimeMillis();
            chatmanager_core$undoStack.push(msg);
        } else if (msg.endsWith(" ")) chatmanager_core$undoStack.push(msg);

        chatmanager_core$redoStack.clear();
    }

    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"))
    public void chatmanager_core$redirectSendChat(ClientPacketListener connection, String message) {
        if (message.length() <= 256) {
            connection.sendChat(message);
            return;
        }

        List<String> chunks = new LinkedList<>();
        String[] messages = message.split(" ");
        StringBuilder chunkBuilder = new StringBuilder();

        for (String msg : messages) {
            int spaceNeeded = !chunkBuilder.isEmpty() ? 1 : 0;

            if (chunkBuilder.length() + spaceNeeded + msg.length() <= 256) {
                if (spaceNeeded > 0) chunkBuilder.append(" ");
                chunkBuilder.append(msg);
            } else {
                chunks.add(chunkBuilder.toString());
                chunkBuilder.setLength(0);
                chunkBuilder.append(msg);
            }
        }

        if (!chunkBuilder.isEmpty()) {
            chunks.add(chunkBuilder.toString());
        }

        ChatQueueManager.getInstance().addMessage(connection, chunks);
    }

    @Redirect(method = "normalizeChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtil;trimChatMessage(Ljava/lang/String;)Ljava/lang/String;"))
    private String chatmanager_core$skipTrim(String string) {
        return string;
    }

    @Unique
    @EventSubscriber(event = StylizeMessageEvent.class)
    public void chatmanager_core$onStyledMessage(StylizeMessageEvent event) {
        if (this.input == null || this.input.getValue().isEmpty()) return;

        chatmanager_core$aiStyle.getStyles().get(event.getStyle()).getMessages().add(AiStyleTextConfig.Message.builder()
                .role("user")
                .content(this.input.getValue())
                .build());

        String body = chatmanager_core$gson.toJson(chatmanager_core$aiStyle.getStyles().get(event.getStyle()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatmanager_core$aiStyle.getApiUrl()))
                .headers(
                        "Authorization", "Bearer " + Objects.requireNonNull(chatmanager_core$aiStyle.getApiKey(), "API key must be specified"),
                        "Content-Type", "application/json"
                )
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        chatmanager_core$log.info("Sending AI stylization request -> URI: " + request.uri() + " | Style: " + event.getStyle() + " | Body: " + body);
        chatmanager_core$client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            int status = response.statusCode();
            if (status >= 400) {
                String msg = "Error " + status + (status == 401 ? ": invalid API key" : status == 429 ? ": rate limit exceeded" : "");
                chatmanager_core$log.error("Text stylization request failed -> " + msg + " | response: " + response.body());
                return;
            }
            JsonObject json = chatmanager_core$gson.fromJson(response.body(), JsonObject.class);
            String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            chatmanager_core$log.info("Text style applied, total tokens used: " + json.getAsJsonObject("usage").get("total_tokens").getAsInt());

            Minecraft.getInstance().execute(() -> this.input.setValue(content));

        }).exceptionally(ex -> {
            chatmanager_core$log.error("Server connection error: " + ex.getMessage());
            return null;
        });
    }

    @Unique
    @EventSubscriber(event = InputEvent.KeyInputEvent.class)
    public void chatmanager_core$onUndoOrRedo(InputEvent.KeyInputEvent event) {
        if (event.getAction() == InputEvent.KeyInputEvent.Action.PRESS && event.isHoldingLeftControl()) {
            if (event.getKeyCode() == GLFW.GLFW_KEY_Z && !chatmanager_core$undoStack.isEmpty()) {
                chatmanager_core$isOperation = true;
                String currentMsg = input.getValue();
                String pastMsg = chatmanager_core$undoStack.pop();
                chatmanager_core$redoStack.push(currentMsg);

                input.setValue(pastMsg);
            }

            if (event.getKeyCode() == GLFW.GLFW_KEY_Y && !chatmanager_core$redoStack.isEmpty()) {
                chatmanager_core$isOperation = true;
                String currentMsg = input.getValue();
                String futureMsg = chatmanager_core$redoStack.pop();
                chatmanager_core$undoStack.push(currentMsg);

                input.setValue(futureMsg);
            }

            chatmanager_core$isOperation = false;
        }
    }
}