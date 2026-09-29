package ru.fozeton.chatmanager.module.gif;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFormat {
    private ImageDetails gif;
    private ImageDetails webp;
    private ImageDetails jpg;
    private ImageDetails mp4;
    private ImageDetails webm;

    public ImageDetails getAnimated() {
        return webp;
    }
}