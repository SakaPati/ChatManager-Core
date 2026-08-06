package ru.fozeton.chatmanager.mixin.chat;

import com.ferra13671.megaevents.eventbus.EventSubscriber;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.config.AiStyleTextConfig;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.events.InputEvent;
import ru.fozeton.chatmanager.events.game.StylizeMessageEvent;
import ru.fozeton.chatmanager.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    private final AiStyleTextConfig chatmanager_core$aiStyle = ChatConfigManager.getInstance().getTextStyleConfig();
    @Unique
    private final Gson chatmanager_core$gson = new Gson();
    @Unique
    private final HttpClient chatmanager_core$client = HttpClient.newHttpClient();
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
    private boolean isOperation = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void chatmanager_core$registerBus(CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void chatmanager_core$unregisterBus(CallbackInfo ci) {
        ChatManagerCore.EVENT_BUS.unregister(this);

        chatmanager_core$undoStack.clear();
        chatmanager_core$redoStack.clear();
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

    @Inject(method = "onEdited", at = @At(value = "HEAD"))
    private void onAddUndoStack(String msg, CallbackInfo ci) {
        if(isOperation || msg.equals(chatmanager_core$undoStack.peek())) return;

        if ((System.currentTimeMillis() - chatmanager_core$lastKeyPressTime) > 2000) {
            chatmanager_core$lastKeyPressTime = System.currentTimeMillis();
            chatmanager_core$undoStack.push(msg);
        } else if (msg.endsWith(" ")) chatmanager_core$undoStack.push(msg);

        chatmanager_core$redoStack.clear();
    }

    @Unique
    @EventSubscriber(event = InputEvent.KeyInputEvent.class)
    public void chatmanager_core$onUndoOrRedo(InputEvent.KeyInputEvent event) {
        if (event.getAction() == InputEvent.KeyInputEvent.Action.PRESS && event.isHoldingLeftControl()) {
            if (event.getKeyCode() == GLFW.GLFW_KEY_Z && !chatmanager_core$undoStack.isEmpty()) {
                isOperation = true;
                String currentMsg = input.getValue();
                String pastMsg = chatmanager_core$undoStack.pop();
                chatmanager_core$redoStack.push(currentMsg);

                input.setValue(pastMsg);
            }

            if (event.getKeyCode() == GLFW.GLFW_KEY_Y && !chatmanager_core$redoStack.isEmpty()) {
                isOperation = true;
                String currentMsg = input.getValue();
                String futureMsg = chatmanager_core$redoStack.pop();
                chatmanager_core$undoStack.push(currentMsg);

                input.setValue(futureMsg);
            }

            isOperation = false;
        }
    }
}