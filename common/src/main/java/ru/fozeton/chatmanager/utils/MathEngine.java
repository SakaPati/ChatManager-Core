package ru.fozeton.chatmanager.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.MathConfig;
import ru.fozeton.chatmanager.utils.math.MathConstantEnum;
import ru.fozeton.chatmanager.utils.math.MathFunctionEnum;

import java.util.*;
import java.util.stream.DoubleStream;

/**
 * A simple mathematical expression evaluator.
 * <p>
 * Usage:
 * double result = MathEngine.getInstance().eval("2+2*3");        // 8.0
 * boolean valid = MathEngine.getInstance().isValid("2+(3*4)");   // true
 * <p>
 * Supports:
 * - Operators: + - * / % ^ (power) ! (factorial), parentheses ()
 * - Implicit multiplication: 2(3) = 6, 2pi = 6.28...
 * - Built-in constants: see {@link MathConstantEnum}
 * - Built-in functions: see {@link MathFunctionEnum}
 * - Custom constants and functions defined in {@link MathConfig}
 */
public final class MathEngine {
    @Getter
    private static final MathEngine instance = new MathEngine();

    private MathEngine() {
    }

    /**
     * Balances an expression by prepending/appending missing parentheses, so that
     * mismatched input like {@code "2+3)"} or {@code "(2+3"} does not fail to parse.
     *
     * @param input raw expression
     * @return the expression padded with the missing parentheses
     */
    private static String fixParenthesis(String input) {
        int open = 0, close = 0;
        for (char c : input.toCharArray()) {
            if (c == '(') open++;
            else if (c == ')') close++;
        }
        return "(".repeat(Math.max(0, close - open)) +
                input +
                ")".repeat(Math.max(0, open - close));
    }

    /**
     * Floating-point modulo that always returns a result with the same sign as {@code b},
     * matching mathematical convention rather than Java's {@code %} operator.
     *
     * @param a dividend
     * @param b divisor
     * @return {@code a mod b}
     */
    public static double mod(double a, double b) {
        return a % b + (((Double.doubleToLongBits(a) >>> 63) ^ (Double.doubleToLongBits(b) >>> 63)) > 0 ? b : 0);
    }

    /**
     * Computes the greatest common factor (divisor) of two numbers via the Euclidean algorithm.
     *
     * @param a first value
     * @param b second value
     * @return the greatest common factor of {@code a} and {@code b}
     */
    public static double gcf(double a, double b) {
        if (b > a) {
            double t = a;
            a = b;
            b = t;
        }

        while (b != 0.0d) {
            double t = b;
            b = mod(a, b);
            a = t;
        }

        return a;
    }

    /**
     * Computes the least common multiple of two numbers.
     *
     * @param a first value
     * @param b second value
     * @return the least common multiple of {@code a} and {@code b}
     */
    public static double lcm(double a, double b) {
        return (a * b) / gcf(a, b);
    }

    /**
     * Computes the factorial of {@code x}. Integer values in [1, 170] are computed exactly;
     * other values (fractional or larger magnitudes) fall back to a Stirling-series approximation.
     *
     * @param x the value to compute the factorial of
     * @return {@code x!}, exact for small integers and approximate otherwise
     */
    public static double factorial(double x) {
        if (x % 1.0d == 0.0d && x >= 1.0d && x <= 170.0d) {
            double result = 1;
            for (int i = 2; i <= (int) x; i++) result *= i;
            return result;
        }
        return Math.sqrt(2.0 * Math.PI * x)
                * Math.pow(x / Math.E, x)
                * (1.0d
                + 1.0d / (12.0d * x)
                + 1.0d / (288.0d * x * x)
                - 139.0d / (51840.0d * x * x * x)
                - 571.0d / (2488320.0d * x * x * x * x)
                + 163879.0d / (209018880.0d * x * x * x * x * x)
                + 5246819.0d / (75246796800.0d * x * x * x * x * x * x)
                - 534703531.0d / (902961561600.0d * x * x * x * x * x * x * x));
    }

    /**
     * Evaluates a mathematical expression.
     *
     * @param input the expression string
     * @return the evaluation result
     * @throws IllegalArgumentException if the expression has a syntax error
     */
    public double eval(String input) {
        MathConfig config = ChatConfigManager.getInstance().getMathConfig();
        Map<String, Double> customConstants = parseConstants(config.getConstants());
        Map<String, CustomFunctionDef> customFunctions = parseFunctions(config.getFunctions());

        Parser parser = new Parser(fixParenthesis(input), config.isRadians(), customConstants, customFunctions, Map.of());
        double result = parser.parseExpression();
        parser.expectEnd();
        return result;
    }

    /**
     * Checks whether an expression is valid without throwing an exception.
     *
     * @param input the string to validate
     * @return true if the expression is valid and evaluates to a finite number; false otherwise
     */
    public boolean isValid(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        try {
            double result = eval(input);
            return !Double.isNaN(result) && !Double.isInfinite(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Collects all available mathematical symbols (built-in functions, built-in constants,
     * and custom constants/functions from {@link MathConfig}) into a single alphabetically sorted list.
     * Functions are automatically suffixed with '(' for convenient auto-completion.
     *
     * @return an alphabetically sorted list of symbol suggestions
     */
    public Collection<String> getAvailableSymbols() {
        Set<String> symbols = new HashSet<>();

        for (MathFunctionEnum f : MathFunctionEnum.values()) symbols.add(f.getCanonicalName() + "(");
        for (MathConstantEnum c : MathConstantEnum.values()) symbols.add(c.getCanonicalName());

        MathConfig config = ChatConfigManager.getInstance().getMathConfig();
        if (config != null) {
            Map<String, Double> customConstants = parseConstants(config.getConstants());
            symbols.addAll(customConstants.keySet());

            Map<String, CustomFunctionDef> customFunctions = parseFunctions(config.getFunctions());
            for (CustomFunctionDef def : customFunctions.values()) symbols.add(def.name() + "(");
        }

        return symbols.stream()
                .sorted()
                .toList();
    }

    /**
     * Parses {@code name=value} lines from the config into a name-to-value map.
     *
     * @param lines raw config lines
     * @return parsed constants, invalid lines are silently skipped
     */
    private Map<String, Double> parseConstants(List<String> lines) {
        Map<String, Double> result = new HashMap<>();
        for (String line : lines) {
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String name = line.substring(0, eq).trim();
            try {
                double value = Double.parseDouble(line.substring(eq + 1).trim());
                result.put(name, value);
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    /**
     * Parses {@code name(params)=expression} lines from the config into function definitions,
     * keyed by {@code name/arity} to support overloading by argument count.
     *
     * @param lines raw config lines
     * @return parsed function definitions, invalid lines are silently skipped
     */
    private Map<String, CustomFunctionDef> parseFunctions(List<String> lines) {
        Map<String, CustomFunctionDef> result = new HashMap<>();
        for (String line : lines) {
            CustomFunctionDef def = CustomFunctionDef.parse(line);
            if (def != null) {
                result.put(def.name() + "/" + def.params().length, def);
            }
        }
        return result;
    }

    /**
     * A user-defined function: its name, parameter names, and the expression body.
     */
    private record CustomFunctionDef(String name, String[] params, String expression) {
        /**
         * Parses a single config line of the form {@code name(param;param)=expression}.
         *
         * @param line raw config line
         * @return the parsed definition, or null if the line is malformed
         */
        static CustomFunctionDef parse(String line) {
            int eq = line.indexOf('=');
            int openParen = line.indexOf('(');
            int closeParen = line.indexOf(')');
            if (openParen < 0 || closeParen < 0 || openParen > closeParen || closeParen > eq) {
                return null;
            }
            String name = line.substring(0, openParen).trim();
            String paramsRaw = line.substring(openParen + 1, closeParen).trim();
            String[] params = paramsRaw.isEmpty() ? new String[0] : paramsRaw.split(";");
            for (int i = 0; i < params.length; i++) params[i] = params[i].trim();
            String expression = line.substring(eq + 1).trim();
            return new CustomFunctionDef(name, params, expression);
        }
    }

    @RequiredArgsConstructor
    private static final class Parser {
        private final String s;
        private final boolean radians;
        private final Map<String, Double> customConstants;
        private final Map<String, CustomFunctionDef> customFunctions;
        private final Map<String, Double> localParams;
        private int pos = 0;

        /**
         * Ensures the whole expression has been consumed, throwing if trailing characters remain.
         */
        void expectEnd() {
            skipSpaces();
            if (pos != s.length()) {
                throw new IllegalArgumentException("Unexpected trailing characters at position " + pos);
            }
        }

        private void skipSpaces() {
            while (pos < s.length() && s.charAt(pos) == ' ') pos++;
        }

        private char peek() {
            skipSpaces();
            return pos < s.length() ? s.charAt(pos) : '\0';
        }

        private boolean eat(char c) {
            skipSpaces();
            if (pos < s.length() && s.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        /**
         * Parses an additive expression: {@code term (('+' | '-') term)*}.
         *
         * @return the evaluated result
         */
        double parseExpression() {
            double x = parseTerm();
            while (true) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        /**
         * Parses a multiplicative term: {@code unary (('*' | '/' | '%' | implicit) unary)*}.
         *
         * @return the evaluated result
         */
        private double parseTerm() {
            double x = parseUnary();
            while (true) {
                if (eat('*')) x *= parseUnary();
                else if (eat('/')) x /= parseUnary();
                else if (eat('%')) x %= parseUnary();
                else if (isImplicitMultiply()) x *= parseUnary();
                else return x;
            }
        }

        /**
         * Detects whether the next token starts an implicit multiplication, e.g. {@code 2pi} or {@code 2(3)}.
         *
         * @return true if the upcoming character implies multiplication
         */
        private boolean isImplicitMultiply() {
            char c = peek();
            return c == '(' || Character.isLetter(c);
        }

        /**
         * Parses a unary expression: optional leading {@code +}/{@code -}, then a power expression.
         *
         * @return the evaluated result
         */
        private double parseUnary() {
            if (eat('-')) return -parseUnary();
            if (eat('+')) return parseUnary();
            return parsePower();
        }

        /**
         * Parses a power expression: {@code factorial ('^' unary)?}. Right-associative.
         *
         * @return the evaluated result
         */
        private double parsePower() {
            double x = parseFactorial();
            if (eat('^')) {
                double exponent = parseUnary();
                return Math.pow(x, exponent);
            }
            return x;
        }

        /**
         * Parses a factorial expression: {@code atom ('!')*}.
         *
         * @return the evaluated result
         */
        private double parseFactorial() {
            double x = parseAtom();
            while (eat('!')) x = factorial(x);
            return x;
        }

        /**
         * Parses the smallest unit of an expression: a parenthesized expression, a number literal,
         * or an identifier (constant, parameter, or function call).
         *
         * @return the evaluated result
         */
        private double parseAtom() {
            skipSpaces();
            if (pos >= s.length()) throw new IllegalArgumentException("Unexpected end of expression");

            char c = s.charAt(pos);

            if (c == '(') {
                pos++;
                double x = parseExpression();
                if (!eat(')')) throw new IllegalArgumentException("Expected closing parenthesis");
                return x;
            }

            if (Character.isDigit(c) || c == '.') {
                int start = pos;
                while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
                return Double.parseDouble(s.substring(start, pos));
            }

            if (Character.isLetter(c)) {
                int start = pos;
                while (pos < s.length() && Character.isLetter(s.charAt(pos))) pos++;
                String name = s.substring(start, pos);

                if (localParams.containsKey(name)) return localParams.get(name);

                MathConstantEnum constant = MathConstantEnum.fromName(name);
                if (constant != null) return constant.getValue(radians);

                if (customConstants.containsKey(name)) return customConstants.get(name);

                if (eat('(')) {
                    List<Double> args = new ArrayList<>();
                    do args.add(parseExpression());
                    while (eat(';'));
                    if (!eat(')')) {
                        throw new IllegalArgumentException("Expected closing parenthesis for function '" + name + "'");
                    }
                    double[] values = new double[args.size()];
                    for (int i = 0; i < values.length; i++) values[i] = args.get(i);
                    return applyFunction(name, values);
                }

                throw new IllegalArgumentException("Unknown identifier: " + name);
            }

            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
        }

        /**
         * Resolves and evaluates a function call, checking built-in functions first, then
         * falling back to custom functions defined in the config.
         *
         * @param name   function name as written in the expression
         * @param values evaluated argument values
         * @return the function's result
         * @throws IllegalArgumentException if the function is unknown or the argument count is invalid
         */
        private double applyFunction(String name, double[] values) {
            MathFunctionEnum function = MathFunctionEnum.fromName(name);
            if (function != null) {
                if (!function.matchesArity(values.length)) {
                    throw new IllegalArgumentException("Function '" + name + "' expects "
                            + function.getMinArgs() + (function.getMinArgs() == function.getMaxArgs() ? "" : ".." + function.getMaxArgs())
                            + " argument(s), got " + values.length);
                }

                return switch (function) {
                    case SQRT -> Math.sqrt(values[0]);
                    case CBRT -> Math.cbrt(values[0]);
                    case SIN -> Math.sin(toRadians(values[0]));
                    case COS -> Math.cos(toRadians(values[0]));
                    case TAN -> Math.tan(toRadians(values[0]));
                    case CSC -> 1.0 / Math.sin(toRadians(values[0]));
                    case SEC -> 1.0 / Math.cos(toRadians(values[0]));
                    case COT -> 1.0 / Math.tan(toRadians(values[0]));
                    case ASIN -> fromRadians(Math.asin(values[0]));
                    case ACOS -> fromRadians(Math.acos(values[0]));
                    case ATAN -> fromRadians(Math.atan(values[0]));
                    case ACSC -> fromRadians(Math.asin(1.0 / values[0]));
                    case ASEC -> fromRadians(Math.acos(1.0 / values[0]));
                    case ACOT -> fromRadians(Math.atan(1.0 / values[0]));
                    case FLOOR -> Math.floor(values[0]);
                    case CEIL -> Math.ceil(values[0]);
                    case ROUND -> Math.floor(values[0] + 0.5d);
                    case ABS -> Math.abs(values[0]);
                    case SGN -> Double.isNaN(values[0]) || values[0] + 0.0 == 0.0 ? 0.0 : (values[0] >= 0.0 ? 1.0 : -1.0);
                    case LOG -> values.length == 1 ? Math.log10(values[0]) : Math.log(values[1]) / Math.log(values[0]);
                    case LN -> Math.log(values[0]);
                    case EXP -> Math.exp(values[0]);
                    case MIN -> DoubleStream.of(values).min().orElseThrow();
                    case MAX -> DoubleStream.of(values).max().orElseThrow();
                    case GCF -> DoubleStream.of(values).reduce(MathEngine::gcf).orElseThrow();
                    case LCM -> DoubleStream.of(values).reduce(MathEngine::lcm).orElseThrow();
                    case CLAMP -> Math.max(values[1], Math.min(values[2], values[0]));
                    case CMP -> {
                        double tolerance = values.length == 2 ? 0.0 : values[2];
                        if (Math.abs(values[0] - values[1]) <= tolerance) yield 0.0d;
                        yield values[0] < values[1] ? -1.0d : 1.0d;
                    }
                };
            }

            CustomFunctionDef def = customFunctions.get(name + "/" + values.length);
            if (def != null) {
                Map<String, Double> params = new HashMap<>();
                for (int i = 0; i < def.params().length; i++) params.put(def.params()[i], values[i]);
                Parser inner = new Parser(fixParenthesis(def.expression()), radians, customConstants, customFunctions, params);
                double result = inner.parseExpression();
                inner.expectEnd();
                return result;
            }

            throw new IllegalArgumentException("Unknown function: " + name + " (with " + values.length + " argument(s))");
        }

        private double toRadians(double x) {
            return radians ? x : Math.toRadians(x);
        }

        private double fromRadians(double x) {
            return radians ? x : Math.toDegrees(x);
        }
    }
}