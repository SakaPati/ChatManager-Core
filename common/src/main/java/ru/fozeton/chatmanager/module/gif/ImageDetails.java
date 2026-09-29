package ru.fozeton.chatmanager.module.gif;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDetails {
    private String url;
    private int width;
    private int height;
    private long size;
}