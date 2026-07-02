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
public class AiStyleTextConfig implements IConfig{
    @Setter(AccessLevel.NONE)
    private final List<String> _comment = List.of(
            "To get the apiKey:",
            "1) Go to console.groq.com",
            "2) Sign up or log in",
            "3) Click 'API Keys' on the right side",
            "4) Click 'Create API Key'",
            "5) Copy the key and paste it below into the apiKey field"
    );
    private String apiKey = "";
    private String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
    private Map<String, AiStyle> styles = new LinkedHashMap<>();

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