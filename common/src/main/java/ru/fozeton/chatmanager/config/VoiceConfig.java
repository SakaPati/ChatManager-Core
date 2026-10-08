package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;

import static ru.fozeton.chatmanager.config.IConfig.parseColor;

@Getter
@Setter
@Config(name = "voice")
public class VoiceConfig implements IConfig {
    private int voiceDelayMs = 8000;
    private String model = "RUSSIAN_SMALL";
    private String backgroundColor = "0xFF18181f";
    private String borderColorListening = "0xFF7c6ef5";
    private String borderColorDefault = "0xFF3a2e6e";
    private String loadingColor = "0xFF3a2e6e";
    private String listeningColor = "0xFFa78bfa";
    private String defaultColor = "0xFFe84d78";

    public int getBackgroundColor() {
        return parseColor(backgroundColor, 0xFF18181F);
    }

    public int getBorderColorListening() {
        return parseColor(borderColorListening, 0xFF7C6EF5);
    }

    public int getBorderColorDefault() {
        return parseColor(borderColorDefault, 0xFF3A2E6E);
    }

    public int getLoadingColor() {
        return parseColor(loadingColor, 0xFF3A2E6E);
    }

    public int getListeningColor() {
        return parseColor(listeningColor, 0xFFA78BFA);
    }

    public int getDefaultColor() {
        return parseColor(defaultColor, 0xFFE84D78);
    }
}