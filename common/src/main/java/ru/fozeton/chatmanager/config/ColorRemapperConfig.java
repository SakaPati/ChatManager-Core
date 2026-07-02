package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ColorRemapperConfig implements IConfig{
    private float saturation = 0.65f;
    private float brightness = 0.75f;
    private Map<String, String> remaps = new LinkedHashMap<>();

    public Integer getRemapColor(int rgb) {
        String key = String.format("0x%06X", rgb);
        String value = getRemaps().get(key);
        if (value == null) return null;
        return Long.decode(value).intValue();
    }
}
