package ru.fozeton.chatmanager.config;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AiStyleTextConfig {
    @Setter(AccessLevel.NONE)
    private final List<String> _comment = List.of(
            "Чтобы получить apiKey:",
            "1) Зайди на сайт console.groq.com",
            "2) Зарегистрируйся",
            "3) Справа нажми API Keys",
            "4) Нажми Create API Key",
            "5) Скопируй ключ и вставь ниже, в поле apiKey"
    );
    private String apiKey = "";
    private String apiUrl = "https://api.groq.com/openai/v1/chat/completions";

    private Map<String, AiStyle> styles = new LinkedHashMap<>();

    protected AiStyleTextConfig createDefault() {
        return new AiStyleTextConfig();
    }

    @Getter
    @Setter
    public static class AiStyle {
        private String model = "llama-3.1-8b-instant";
        @SerializedName("max_tokens")
        private int maxTokens = 80;
        private List<Message> messages = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}