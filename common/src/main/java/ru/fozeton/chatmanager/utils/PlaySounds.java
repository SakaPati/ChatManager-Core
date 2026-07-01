package ru.fozeton.chatmanager.utils;

import dev.architectury.platform.Platform;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlaySounds {
    @Getter
    private static final PlaySounds instance = new PlaySounds();
    private final Logger log = new Logger(PlaySounds.class);

    public void playerSound(String soundFilePath) {
        try {
            Path path = Platform.getMod("chatmanager_core")
                    .findResource("assets/chatmanager_core/sounds/" + soundFilePath)
                    .orElse(null);

            if (path == null) {
                log.error("Path not found: " + soundFilePath);
                return;
            }

            try (java.io.InputStream is = Files.newInputStream(path);
                 java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
                 AudioInputStream audioIn = AudioSystem.getAudioInputStream(bis)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            }
        } catch (Exception e) {
            log.error("Playback error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
