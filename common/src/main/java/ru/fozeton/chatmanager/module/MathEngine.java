package ru.fozeton.chatmanager.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.fozeton.chatmanager.config.ChatConfigManager;
import ru.fozeton.chatmanager.config.MathConfig;
import ru.fozeton.chatmanager.exceptions.ExpressionException;
import ru.fozeton.chatmanager.module.math.MathConstantEnum;
import ru.fozeton.chatmanager.module.math.MathFunctionEnum;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathEngine {
    @Getter
    private static final MathEngine instance = new MathEngine();

    /**
     * Balances an expression by prepending/appending missing parentheses, so that
     * mismatched input like {@code "2+3)"} or {@code "(2+3"} does not fail to parse.
     *
     * @param input raw expression
     * @return the expression padded with the missing parentheses
     * @throws ExpressionException if the input is null
     */
    private static String fixParenthesis(String input) {
        if (input == null) {
            throw new ExpressionException("Expression is null");
        }

        int openCount = 0;
        int closeCount = 0;
        for (char symbol : input.toCharArray()) {
            if (symbol == '(') openCount++;
            else if (symbol == ')') closeCount++;
        }

        return "(".repeat(Math.max(0, closeCount - openCount))
               + input
               + ")".repeat(Math.max(0, openCount - closeCount));
    }

    /**
     * Floating-point modulo that always returns a result with the same sign as {@code divisor},
     * matching mathematical convention rather than Java's {@code %} operator.
     *
     * @param dividend the dividend
     * @param divisor  the divisor
     * @return {@code dividend mod divisor}
     */
    public static double mod(double dividend, double divisor) {
        double remainder = dividend % divisor;
        if (remainder != 0.0d && (remainder < 0.0d) != (divisor < 0.0d)) {
            remainder += divisor;
        }
        return remainder;
    }

    /**
     * Computes the greatest common factor (divisor) of two numbers via the Euclidean algorithm.
     * The result is always non-negative; non-finite input yields NaN.
     *
     * @param first  first value
     * @param second second value
     * @return the greatest common factor of {@code first} and {@code second}
     */
    public static double gcf(double first, double second) {
        // NaN would make the loop below never terminate
        if (!Double.isFinite(first) || !Double.isFinite(second)) {
            return Double.NaN;
        }

        double larger = Math.abs(first);
        double smaller = Math.abs(second);
        if (smaller > larger) {
            double swap = larger;
            larger = smaller;
            smaller = swap;
        }

        while (smaller != 0.0d) {
            double previousSmaller = smaller;
            smaller = mod(larger, smaller);
            larger = previousSmaller;
        }

        return larger;
    }

    /**
     * Computes the least common multiple of two numbers. The result is always non-negative.
     *
     * @param first  first value
     * @param second second value
     * @return the least common multiple of {@code first} and {@code second}
     */
    public static double lcm(double first, double second) {
        return Math.abs(first * second) / gcf(first, second);
    }

    /**
     * Computes the factorial of {@code value}. Integers in [0, 170] are computed exactly;
     * fractional values use a shifted Stirling-series approximation.
     *
     * @param value the value to compute the factorial of
     * @return {@code value!}; NaN for negative or NaN input, infinity when the result overflows
     */
    public static double factorial(double value) {
        if (Double.isNaN(value) || value < 0.0d) {
            return Double.NaN;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }

        if (value % 1.0d == 0.0d) {
            if (value > 170.0d) {
                return Double.POSITIVE_INFINITY;
            }
            double result = 1.0d;
            for (int factor = 2; factor <= (int) value; factor++) {
                result *= factor;
            }
            return result; // 0! and 1! are both 1
        }

        // The Stirling series is only accurate for large arguments, so shift the argument up to 15
        // using x! = (x+n)! / ((x+1)(x+2)...(x+n)).
        double denominator = 1.0d;
        double shifted = value;
        while (shifted < 15.0d) {
            shifted += 1.0d;
            denominator *= shifted;
        }
        return stirling(shifted) / denominator;
    }

    /**
     * Stirling series approximation of {@code value!}. Accurate for large {@code value}.
     */
    private static double stirling(double value) {
        double value2 = value * value;
        double value3 = value2 * value;
        double value4 = value3 * value;
        double value5 = value4 * value;
        double value6 = value5 * value;
        double value7 = value6 * value;

        return Math.sqrt(2.0d * Math.PI * value)
               * Math.pow(value / Math.E, value)
               * (1.0d
                  + 1.0d / (12.0d * value)
                  + 1.0d / (288.0d * value2)
                  - 139.0d / (51840.0d * value3)
                  - 571.0d / (2488320.0d * value4)
                  + 163879.0d / (209018880.0d * value5)
                  + 5246819.0d / (75246796800.0d * value6)
                  - 534703531.0d / (902961561600.0d * value7));
    }

    /**
     * Returns the sign of {@code value}: -1, 0 or 1. NaN and zero (including -0.0) both give 0.
     *
     * @param value the value to inspect
     * @return the sign of {@code value}
     */
    private static double sign(double value) {
        if (Double.isNaN(value) || value == 0.0d) {
            return 0.0d;
        }
        if (value > 0.0d) {
            return 1.0d;
        }
        return -1.0d;
    }

    /**
     * Restricts {@code value} to the range [{@code min}, {@code max}] using {@link Math#clamp(double, double, double)}.
     * A NaN {@code value} stays NaN; a NaN bound gives NaN.
     *
     * @param value the value to restrict
     * @param min   the lower bound
     * @param max   the upper bound
     * @return the restricted value
     * @throws ExpressionException if {@code min} is greater than {@code max}
     */
    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max)) {
            return Double.NaN;
        }
        if (min > max) {
            throw new ExpressionException("clamp: min (" + min + ") must not be greater than max (" + max + ")");
        }
        return Math.clamp(value, min, max);
    }

    /**
     * Evaluates a mathematical expression.
     *
     * @param input the expression string
     * @return the evaluation result
     * @throws ExpressionException if the expression has a syntax error
     */
    public double eval(String input) {
        MathConfig config = ChatConfigManager.getInstance().getMathConfig();
        Map<String, Double> customConstants = parseConstants(config.getConstants());
        Map<String, CustomFunctionDef> customFunctions = parseFunctions(config.getFunctions());

        Parser parser = new Parser(
                fixParenthesis(input),
                config.isRadians(),
                customConstants,
                customFunctions,
                Map.of()
        );
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
        } catch (Exception | StackOverflowError ignored) {
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

        for (MathFunctionEnum function : MathFunctionEnum.values()) {
            symbols.add(function.getCanonicalName() + "(");
        }
        for (MathConstantEnum constant : MathConstantEnum.values()) {
            symbols.add(constant.getCanonicalName());
        }

        MathConfig config = ChatConfigManager.getInstance().getMathConfig();
        if (config != null) {
            Map<String, Double> customConstants = parseConstants(config.getConstants());
            symbols.addAll(customConstants.keySet());

            Map<String, CustomFunctionDef> customFunctions = parseFunctions(config.getFunctions());
            for (CustomFunctionDef definition : customFunctions.values()) {
                symbols.add(definition.name() + "(");
            }
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
        Map<String, Double> constants = new HashMap<>();
        for (String line : lines) {
            int equalsIndex = line.indexOf('=');
            if (equalsIndex < 0) continue;

            String name = line.substring(0, equalsIndex).trim();
            try {
                double value = Double.parseDouble(line.substring(equalsIndex + 1).trim());
                constants.put(name, value);
            } catch (NumberFormatException ignored) {
            }
        }
        return constants;
    }

    /**
     * Parses {@code name(params)=expression} lines from the config into function definitions,
     * keyed by {@code name/arity} to support overloading by argument count.
     *
     * @param lines raw config lines
     * @return parsed function definitions, invalid lines are silently skipped
     */
    private Map<String, CustomFunctionDef> parseFunctions(List<String> lines) {
        Map<String, CustomFunctionDef> functions = new HashMap<>();
        for (String line : lines) {
            CustomFunctionDef definition = CustomFunctionDef.parse(line);
            if (definition != null) {
                functions.put(definition.name() + "/" + definition.params().length, definition);
            }
        }
        return functions;
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
            int equalsIndex = line.indexOf('=');
            int openParenIndex = line.indexOf('(');
            int closeParenIndex = line.indexOf(')');
            if (equalsIndex < 0 || openParenIndex < 0 || closeParenIndex < 0
                || openParenIndex > closeParenIndex || closeParenIndex > equalsIndex) {
                return null;
            }

            String name = line.substring(0, openParenIndex).trim();
            String rawParams = line.substring(openParenIndex + 1, closeParenIndex).trim();
            String[] params = rawParams.isEmpty() ? new String[0] : rawParams.split(";");
            for (int index = 0; index < params.length; index++) {
                params[index] = params[index].trim();
            }
            String expression = line.substring(equalsIndex + 1).trim();
            return new CustomFunctionDef(name, params, expression);
        }
    }

    // NOTE: the field order of the final fields below defines the constructor signature
    // (String, boolean, Map, Map, Map), which the unit tests access via reflection.
    @RequiredArgsConstructor
    private static final class Parser {
        private static final int MAX_NESTING_DEPTH = 64;
        private static final int MAX_CALL_DEPTH = 32;

        private final String source;
        private final boolean radians;
        private final Map<String, Double> customConstants;
        private final Map<String, CustomFunctionDef> customFunctions;
        private final Map<String, Double> localParams;

        // Non-final with initializers, so Lombok does not include them in the constructor.
        private int position = 0;
        private int nestingDepth = 0;
        private int callDepth = 0;

        /**
         * Ensures the whole expression has been consumed, throwing if trailing characters remain.
         */
        void expectEnd() {
            skipSpaces();
            if (position != source.length()) {
                throw new ExpressionException("Unexpected trailing characters at position " + position);
            }
        }

        private void skipSpaces() {
            while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
                position++;
            }
        }

        private char peek() {
            skipSpaces();
            return position < source.length() ? source.charAt(position) : '\0';
        }

        private boolean eat(char expected) {
            skipSpaces();
            if (position < source.length() && source.charAt(position) == expected) {
                position++;
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
            double result = parseTerm();
            while (true) {
                if (eat('+')) result += parseTerm();
                else if (eat('-')) result -= parseTerm();
                else return result;
            }
        }

        /**
         * Parses a multiplicative term: {@code unary (('*' | '/' | '%' | implicit) unary)*}.
         *
         * @return the evaluated result
         */
        private double parseTerm() {
            double result = parseUnary();
            while (true) {
                if (eat('*')) result *= parseUnary();
                else if (eat('/')) result /= parseUnary();
                else if (eat('%')) result %= parseUnary();
                else if (isImplicitMultiply()) result *= parseUnary();
                else return result;
            }
        }

        /**
         * Detects whether the next token starts an implicit multiplication, e.g. {@code 2pi} or {@code 2(3)}.
         *
         * @return true if the upcoming character implies multiplication
         */
        private boolean isImplicitMultiply() {
            char next = peek();
            return next == '(' || Character.isLetter(next);
        }

        /**
         * Parses a unary expression: optional leading {@code +}/{@code -}, then a power expression.
         * Every nested expression passes through here, so this is where recursion depth is limited.
         *
         * @return the evaluated result
         */
        private double parseUnary() {
            if (++nestingDepth > MAX_NESTING_DEPTH) {
                throw new ExpressionException("Expression is nested too deeply");
            }
            try {
                if (eat('-')) return -parseUnary();
                if (eat('+')) return parseUnary();
                return parsePower();
            } finally {
                nestingDepth--;
            }
        }

        /**
         * Parses a power expression: {@code factorial ('^' unary)?}. Right-associative.
         *
         * @return the evaluated result
         */
        private double parsePower() {
            double base = parseFactorial();
            if (eat('^')) {
                double exponent = parseUnary();
                return Math.pow(base, exponent);
            }
            return base;
        }

        /**
         * Parses a factorial expression: {@code atom ('!')*}.
         *
         * @return the evaluated result
         */
        private double parseFactorial() {
            double result = parseAtom();
            while (eat('!')) {
                result = factorial(result);
            }
            return result;
        }

        /**
         * Parses the smallest unit of an expression: a parenthesized expression, a number literal,
         * or an identifier (constant, parameter, or function call).
         *
         * @return the evaluated result
         */
        private double parseAtom() {
            skipSpaces();
            if (position >= source.length()) {
                throw new ExpressionException("Unexpected end of expression");
            }

            char current = source.charAt(position);

            if (current == '(') {
                position++;
                double inner = parseExpression();
                if (!eat(')')) {
                    throw new ExpressionException("Expected closing parenthesis");
                }
                return inner;
            }

            if (Character.isDigit(current) || current == '.') {
                return parseNumber();
            }

            if (Character.isLetter(current)) {
                return parseIdentifier();
            }

            throw new ExpressionException("Unexpected character '" + current + "' at position " + position);
        }

        /**
         * Parses a number literal such as {@code 42}, {@code 3.14} or {@code .5}.
         *
         * @return the parsed value
         * @throws ExpressionException if the literal is malformed (e.g. {@code 1.2.3} or a lone {@code .})
         */
        private double parseNumber() {
            int start = position;
            while (position < source.length()
                   && (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) {
                position++;
            }

            String literal = source.substring(start, position);
            try {
                return Double.parseDouble(literal);
            } catch (NumberFormatException exception) {
                throw new ExpressionException("Invalid number '" + literal + "' at position " + start);
            }
        }

        /**
         * Parses an identifier: a local parameter, a constant, or a function call.
         *
         * @return the evaluated result
         */
        private double parseIdentifier() {
            int start = position;
            while (position < source.length() && Character.isLetter(source.charAt(position))) {
                position++;
            }
            String name = source.substring(start, position);

            if (localParams.containsKey(name)) {
                return localParams.get(name);
            }

            MathConstantEnum constant = MathConstantEnum.fromName(name);
            if (constant != null) {
                return constant.getValue(radians);
            }

            if (customConstants.containsKey(name)) {
                return customConstants.get(name);
            }

            if (eat('(')) {
                List<Double> arguments = new ArrayList<>();
                do {
                    arguments.add(parseExpression());
                } while (eat(';'));

                if (!eat(')')) {
                    throw new ExpressionException("Expected closing parenthesis for function '" + name + "'");
                }

                double[] argumentValues = new double[arguments.size()];
                for (int index = 0; index < argumentValues.length; index++) {
                    argumentValues[index] = arguments.get(index);
                }
                return applyFunction(name, argumentValues);
            }

            throw new ExpressionException("Unknown identifier: " + name);
        }

        /**
         * Resolves and evaluates a function call, checking built-in functions first, then
         * falling back to custom functions defined in the config.
         *
         * @param name   function name as written in the expression
         * @param values evaluated argument values
         * @return the function's result
         * @throws ExpressionException if the function is unknown or the argument count is invalid
         */
        private double applyFunction(String name, double[] values) {
            MathFunctionEnum function = MathFunctionEnum.fromName(name);
            if (function != null) {
                if (!function.matchesArity(values.length)) {
                    throw new ExpressionException("Function '" + name + "' expects "
                                                  + function.getMinArgs()
                                                  + (function.getMinArgs() == function.getMaxArgs() ? "" :
                            ".." + function.getMaxArgs())
                                                  + " argument(s), got " + values.length);
                }

                return switch (function) {
                    case SQRT -> Math.sqrt(values[0]);
                    case CBRT -> Math.cbrt(values[0]);
                    case SIN -> Math.sin(toRadians(values[0]));
                    case COS -> Math.cos(toRadians(values[0]));
                    case TAN -> Math.tan(toRadians(values[0]));
                    case CSC -> 1.0d / Math.sin(toRadians(values[0]));
                    case SEC -> 1.0d / Math.cos(toRadians(values[0]));
                    case COT -> 1.0d / Math.tan(toRadians(values[0]));
                    case ASIN -> fromRadians(Math.asin(values[0]));
                    case ACOS -> fromRadians(Math.acos(values[0]));
                    case ATAN -> fromRadians(Math.atan(values[0]));
                    case ACSC -> fromRadians(Math.asin(1.0d / values[0]));
                    case ASEC -> fromRadians(Math.acos(1.0d / values[0]));
                    case ACOT -> fromRadians(Math.atan(1.0d / values[0]));
                    case FLOOR -> Math.floor(values[0]);
                    case CEIL -> Math.ceil(values[0]);
                    case ROUND -> Math.floor(values[0] + 0.5d);
                    case ABS -> Math.abs(values[0]);
                    case SGN -> sign(values[0]);
                    case LOG -> values.length == 1
                            ? Math.log10(values[0])
                            : Math.log(values[1]) / Math.log(values[0]);
                    case LN -> Math.log(values[0]);
                    case EXP -> Math.exp(values[0]);
                    case MIN -> DoubleStream.of(values).min().orElseThrow();
                    case MAX -> DoubleStream.of(values).max().orElseThrow();
                    case GCF -> DoubleStream.of(values).reduce(MathEngine::gcf).orElseThrow();
                    case LCM -> DoubleStream.of(values).reduce(MathEngine::lcm).orElseThrow();
                    case CLAMP -> clamp(values[0], values[1], values[2]);
                    case CMP -> {
                        double tolerance = values.length == 2 ? 0.0d : values[2];
                        if (Math.abs(values[0] - values[1]) <= tolerance) yield 0.0d;
                        yield values[0] < values[1] ? -1.0d : 1.0d;
                    }
                };
            }

            CustomFunctionDef definition = customFunctions.get(name + "/" + values.length);
            if (definition != null) {
                if (callDepth >= MAX_CALL_DEPTH) {
                    throw new ExpressionException("Function call depth exceeded in '" + name + "'");
                }

                Map<String, Double> parameters = new HashMap<>();
                for (int index = 0; index < definition.params().length; index++) {
                    parameters.put(definition.params()[index], values[index]);
                }

                Parser innerParser = new Parser(
                        fixParenthesis(definition.expression()), radians,
                        customConstants, customFunctions, parameters
                );
                innerParser.callDepth = callDepth + 1;

                double result = innerParser.parseExpression();
                innerParser.expectEnd();
                return result;
            }

            throw new ExpressionException("Unknown function: " + name + " (with " + values.length + " argument(s))");
        }

        private double toRadians(double angle) {
            return radians ? angle : Math.toRadians(angle);
        }

        private double fromRadians(double angle) {
            return radians ? angle : Math.toDegrees(angle);
        }
    }
}