package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;

@Getter
@Setter
@Config(name = "voice")
public class VoiceConfig implements IConfig{
    private int voiceDelayMs = 8000;
    private String model = "RUSSIAN_SMALL";
    private String backgroundColor = "0xFF18181f";
    private String borderColorListening = "0xFF7c6ef5";
    private String borderColorDefault = "0xFF3a2e6e";
    private String loadingColor = "0xFF3a2e6e";
    private String listeningColor = "0xFFa78bfa";
    private String defaultColor = "0xFFe84d78";
}
