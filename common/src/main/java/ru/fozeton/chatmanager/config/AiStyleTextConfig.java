package ru.fozeton.chatmanager.config;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Config(name = "aiStyleText")
public class AiStyleTextConfig implements IConfig {
    @Comment("To get the apiKey: 1) Go to console.groq.com; 2) Sign up or log in; 3) Click API Keys on the right side; 4) Click Create API Key; 5) Copy the key and paste it below into the apiKey field")
    private String apiKey = "";
    private String apiUrl = "https://api.groq.com/openai/v1/chat/completions";

    @ConfigEntry.Gui.Excluded
    private Map<String, AiStyle> styles = new LinkedHashMap<>();

    @Override
    public void applyDefaults() {
        AiStyle literate = new AiStyle();
        literate.setModel("llama-3.3-70b-versatile");
        literate.setMaxTokens(80);
        literate.getMessages().add(
                Message.builder()
                        .role("system")
                        .content(
                                "Correct the spelling, punctuation, and grammar in my text. Do not change the meaning, words, or structure—only fix the errors. Reply ONLY with the corrected text, without any explanations.")
                        .build()
        );
        styles.put("Literate", literate);
    }

    @Getter
    @Setter
    public static class AiStyle {
        private String model = "llama-3.1-8b-instant";
        @SerializedName("max_tokens")
        private int maxTokens = 80;
        @ConfigEntry.Gui.Excluded
        private List<Message> messages = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}