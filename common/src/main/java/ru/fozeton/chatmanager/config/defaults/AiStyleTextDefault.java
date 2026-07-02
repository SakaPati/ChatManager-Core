package ru.fozeton.chatmanager.config.defaults;

import ru.fozeton.chatmanager.config.AiStyleTextConfig;

public class AiStyleTextDefault implements Default<AiStyleTextConfig> {

    @Override
    public AiStyleTextConfig createDefault() {
        AiStyleTextConfig config = new AiStyleTextConfig();

        AiStyleTextConfig.AiStyle literate = new AiStyleTextConfig.AiStyle();
        literate.setModel("llama-3.3-70b-versatile");
        literate.setMaxTokens(80);
        literate.getMessages().add(AiStyleTextConfig.Message.builder().role("system").content("Correct the spelling, punctuation, and grammar in my text. Do not change the meaning, words, or structure—only fix the errors. Reply ONLY with the corrected text, without any explanations.").build());
        config.getStyles().put("Literate", literate);

        return config;
    }

    @Override
    public boolean isEmpty(AiStyleTextConfig config) {
        return config == null || config.getStyles() == null || config.getStyles().isEmpty();
    }
}
