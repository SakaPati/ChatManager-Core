package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Config(name = "math")
public class MathConfig implements IConfig {
    private boolean radians = false;
    private String decimalFormat = "#,##0.##";
    private char prefix = '=';

    @ConfigEntry.Gui.Excluded
    private List<String> constants = new ArrayList<>();
    @ConfigEntry.Gui.Excluded
    private List<String> functions = new ArrayList<>();

    @Override
    public void applyDefaults() {
        functions.add("area(w;h)=w*h");
    }
}