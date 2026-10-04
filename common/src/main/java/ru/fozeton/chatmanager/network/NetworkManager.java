package ru.fozeton.chatmanager.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import ru.fozeton.chatmanager.messages.Message;

import java.net.http.HttpClient;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetworkManager {
    @Getter
    private static final NetworkManager instance = new NetworkManager();
    private final HttpClient client = HttpClient.newHttpClient();

    @Setter
    @Nullable
    private WebHooks webHooks;

    public void dispatcher(Message message) {
        if (webHooks != null) webHooks.onWebHook(message);
    }
}
