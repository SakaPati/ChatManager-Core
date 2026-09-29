package ru.fozeton.chatmanager.utils;

import lombok.Getter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class PlaySounds {
    @Getter
    private static final PlaySounds instance = new PlaySounds();
    private final Logger log = new Logger(PlaySounds.class);

    public void playerSound(String soundFilePath) {
        String resource = "/assets/chatmanager_core/sounds/" + soundFilePath;
        try (InputStream is = PlaySounds.class.getResourceAsStream(resource)) {
            if (is == null) {
                log.error("Path not found: " + soundFilePath);
                return;
            }
            try (BufferedInputStream bis = new java.io.BufferedInputStream(is);
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
