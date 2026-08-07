package ru.fozeton.chatmanager.utils.math;

import lombok.Getter;

import java.util.*;

/**
 * Built-in calculator functions: canonical name, aliases and accepted argument count range.
 */
@Getter
public enum MathFunctionEnum {
    SQRT(1, 1, "sqrt"),
    CBRT(1, 1, "cbrt"),
    SIN(1, 1, "sin"),
    COS(1, 1, "cos"),
    TAN(1, 1, "tan"),
    CSC(1, 1, "csc"),
    SEC(1, 1, "sec"),
    COT(1, 1, "cot"),
    ASIN(1, 1, "asin", "arcsin"),
    ACOS(1, 1, "acos", "arccos"),
    ATAN(1, 1, "atan", "arctan"),
    ACSC(1, 1, "acsc", "arccsc"),
    ASEC(1, 1, "asec", "arcsec"),
    ACOT(1, 1, "acot", "arccot"),
    FLOOR(1, 1, "floor"),
    CEIL(1, 1, "ceil"),
    ROUND(1, 1, "round"),
    ABS(1, 1, "abs"),
    SGN(1, 1, "sgn"),
    LOG(1, 2, "log"),
    LN(1, 1, "ln"),
    EXP(1, 1, "exp"),
    MIN(1, Integer.MAX_VALUE, "min"),
    MAX(1, Integer.MAX_VALUE, "max"),
    GCF(1, Integer.MAX_VALUE, "gcf"),
    LCM(1, Integer.MAX_VALUE, "lcm"),
    CLAMP(3, 3, "clamp"),
    CMP(2, 3, "cmp");

    private final int minArgs;
    private final int maxArgs;
    private final String canonicalName;
    private final String[] aliases;

    MathFunctionEnum(int minArgs, int maxArgs, String canonicalName, String... aliases) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.canonicalName = canonicalName;
        this.aliases = aliases;
    }

    /**
     * Returns all names this function can be called by, including its canonical name and aliases.
     *
     * @return list of valid names for this function
     */
    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        names.add(canonicalName);
        names.addAll(Arrays.asList(aliases));
        return names;
    }

    /**
     * Checks whether the given number of arguments is accepted by this function.
     *
     * @param count number of arguments passed at the call site
     * @return true if {@code count} is within [minArgs, maxArgs]
     */
    public boolean matchesArity(int count) {
        return count >= minArgs && count <= maxArgs;
    }

    private static final Map<String, MathFunctionEnum> LOOKUP = new HashMap<>();

    static {
        for (MathFunctionEnum function : values()) {
            for (String alias : function.getAllNames()) LOOKUP.put(alias, function);
        }
    }

    /**
     * Resolves a function by its canonical name or one of its aliases.
     *
     * @param name identifier as written in the expression
     * @return the matching function, or null if no built-in function has this name
     */
    public static MathFunctionEnum fromName(String name) {
        return LOOKUP.get(name);
    }
}