package ru.fozeton.chatmanager.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MathConfig implements IConfig {
    private boolean radians = false;
    private String decimalFormat = "#,##0.##";
    private char prefix = '=';

    private List<String> constants = new ArrayList<>();
    private List<String> functions = new ArrayList<>();
}