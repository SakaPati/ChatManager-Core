package ru.fozeton.chatmanager.module.math;

import lombok.Getter;

import java.util.*;

/**
 * Built-in mathematical constants supported by the calculator.
 */
@Getter
public enum MathConstantEnum {
    PI("pi"),
    E("e"),
    TAU("tau"),
    PHI("phi"),
    RANDOM("random", "rand"),
    RAD("rad"),
    DEG("deg");

    private final String canonicalName;
    private final String[] aliases;

    MathConstantEnum(String canonicalName, String... aliases) {
        this.canonicalName = canonicalName;
        this.aliases = aliases;
    }

    /**
     * Returns all names this constant can be referenced by, including its canonical name and aliases.
     *
     * @return list of valid names for this constant
     */
    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        names.add(canonicalName);
        names.addAll(Arrays.asList(aliases));
        return names;
    }

    /**
     * Resolves the numeric value of this constant.
     * RAD and DEG are angle-unit conversion factors and depend on the current mode.
     *
     * @param radians whether the engine is currently operating in radians mode
     * @return the numeric value of the constant
     */
    public double getValue(boolean radians) {
        return switch (this) {
            case PI -> Math.PI;
            case E -> Math.E;
            case TAU -> 2.0 * Math.PI;
            case PHI -> 1.6180339887498948482;
            case RANDOM -> Math.random();
            case RAD -> radians ? 1.0 : 57.29577951308232;
            case DEG -> radians ? 0.017453292519943295 : 1.0;
        };
    }

    private static final Map<String, MathConstantEnum> LOOKUP = new HashMap<>();

    static {
        for (MathConstantEnum constant : values()) {
            for (String alias : constant.getAllNames()) LOOKUP.put(alias, constant);
        }
    }

    /**
     * Resolves a constant by its canonical name or one of its aliases.
     *
     * @param name identifier as written in the expression
     * @return the matching constant, or null if no built-in constant has this name
     */
    public static MathConstantEnum fromName(String name) {
        return LOOKUP.get(name);
    }
}