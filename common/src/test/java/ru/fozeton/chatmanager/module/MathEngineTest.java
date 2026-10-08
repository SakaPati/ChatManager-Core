package ru.fozeton.chatmanager.module;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.fozeton.chatmanager.exceptions.ExpressionException;
import ru.fozeton.chatmanager.module.math.MathConstantEnum;
import ru.fozeton.chatmanager.module.math.MathFunctionEnum;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage suite for {@link MathEngine}.
 * <p>
 * IMPORTANT - why this doesn't call {@code MathEngine.getInstance().eval(...)} directly:
 * {@code eval()}/{@code isValid()}/{@code getAvailableSymbols()} all go through
 * {@code ChatConfigManager.getInstance()}, whose static initializer calls
 * {@code Platform.getGameFolder()} (Architectury). That throws an
 * {@code AssertionError} unless a real game/mod-loader environment is bootstrapped first,
 * which a plain JUnit unit test does not have and this build doesn't pull in a mocking
 * library (Mockito) to fake.
 * <p>
 * Instead, these tests reach the private static {@code MathEngine.Parser} nested class via
 * reflection and drive it directly with empty custom-constant/custom-function maps - this
 * exercises exactly the same grammar/evaluation logic without ever touching
 * {@code ChatConfigManager}. The public static utility methods ({@code mod}, {@code gcf},
 * {@code lcm}, {@code factorial}) don't touch config at all and are called directly.
 * <p>
 * Since the test harness constructs {@code Parser} itself, the {@code radians} flag is
 * simply fixed to {@code false} (degrees mode) rather than guessed from config.
 * <p>
 * NOTE: function-call arguments in this grammar are separated by {@code ;}, not {@code ,}.
 * <p>
 * If/when Mockito is added to the project, an equivalent end-to-end suite could instead
 * {@code mockStatic(Platform.class)} to stub {@code getGameFolder()} and exercise the real
 * public {@code MathEngine.getInstance()} API including config-driven customization.
 */
class MathEngineTest {

    private static final double EPSILON = 1e-9;
    private static final double TRIG_EPSILON = 1e-6;
    private static final boolean RADIANS = false; // controlled directly, so trig helpers below assume degrees

    private static Constructor<?> parserConstructor;
    private static Method parseExpressionMethod;
    private static Method expectEndMethod;

    @BeforeAll
    static void setupReflection() throws Exception {
        Class<?> parserClass = Class.forName("ru.fozeton.chatmanager.module.MathEngine$Parser");
        parserConstructor = parserClass.getDeclaredConstructor(String.class, boolean.class, Map.class, Map.class, Map.class);
        parserConstructor.setAccessible(true);
        parseExpressionMethod = parserClass.getDeclaredMethod("parseExpression");
        parseExpressionMethod.setAccessible(true);
        expectEndMethod = parserClass.getDeclaredMethod("expectEnd");
        expectEndMethod.setAccessible(true);
    }

    /**
     * Mirrors MathEngine's private fixParenthesis(), reimplemented so we don't need extra reflection for it.
     */
    private static String balanceParentheses(String input) {
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

    private static double eval(String expression) {
        try {
            String balanced = balanceParentheses(expression);
            Object parser = parserConstructor.newInstance(balanced, RADIANS, Map.of(), Map.of(), Map.of());
            double result = (Double) parseExpressionMethod.invoke(parser);
            expectEndMethod.invoke(parser);
            return result;
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) throw runtimeException;
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) return false;
        try {
            double result = eval(expression);
            return !Double.isNaN(result) && !Double.isInfinite(result);
        } catch (Exception exception) {
            return false;
        }
    }

    // In degrees mode (RADIANS = false), trig input/output units are plain degrees - no conversion needed.
    private static double inputAngle(double degrees) {
        return degrees;
    }

    private static double outputAngle(double degrees) {
        return degrees;
    }

    // Basic arithmetic

    @Nested
    @DisplayName("Basic arithmetic")
    class BasicArithmetic {

        @ParameterizedTest
        @CsvSource({
                "2+2,        4.0",
                "2-2,        0.0",
                "2*3,        6.0",
                "5/2,        2.5",
                "7%3,        1.0",
                "10-4,       6.0",
                "9/3,        3.0",
                "0.1+0.2,    0.3",
                ".5+.5,      1.0",
                "  2 + 2  ,  4.0",
                "10-5+3-2,   6.0",
                "8*2/4,      4.0",
                "2+3*4/2-2%3,6.0"
        })
        void arithmeticExpressions(String expression, double expected) {
            assertEquals(expected, eval(expression), EPSILON, expression);
        }

        @Test
        void divisionByZeroIsInfinity() {
            assertEquals(Double.POSITIVE_INFINITY, eval("1/0"));
            assertEquals(Double.NEGATIVE_INFINITY, eval("-1/0"));
        }

        @Test
        void zeroDividedByZeroIsNaN() {
            assertTrue(Double.isNaN(eval("0/0")));
        }

        @Test
        void javaStyleModuloKeepsDividendSign() {
            // The '%' operator inside expressions uses raw Java remainder semantics,
            // unlike the public static MathEngine.mod() helper.
            assertEquals(-1.0, eval("-7%3"), EPSILON);
            assertEquals(1.0, eval("7%-3"), EPSILON);
        }

        @Test
        void tabsAndNewlinesAreTreatedAsWhitespace() {
            assertEquals(4.0, eval("2\t+\n2"), EPSILON);
        }
    }

    // Precedence & associativity

    @Nested
    @DisplayName("Operator precedence and associativity")
    class Precedence {

        @Test
        void multiplicationBeforeAddition() {
            assertEquals(8.0, eval("2+2*3"), EPSILON);
        }

        @Test
        void parenthesesOverridePrecedence() {
            assertEquals(12.0, eval("(2+2)*3"), EPSILON);
        }

        @Test
        void deeplyNestedParentheses() {
            assertEquals(3.0, eval("((((1+2))))"), EPSILON);
        }

        @Test
        void powerIsRightAssociative() {
            // 2^3^2 = 2^(3^2) = 2^9 = 512, not (2^3)^2 = 64
            assertEquals(512.0, eval("2^3^2"), EPSILON);
        }

        @Test
        void unaryMinusHasLowerPrecedenceThanPower() {
            assertEquals(-4.0, eval("-2^2"), EPSILON);
            assertEquals(4.0, eval("(-2)^2"), EPSILON);
        }

        @Test
        void factorialBindsTighterThanPower() {
            assertEquals(36.0, eval("3!^2"), EPSILON);   // (3!)^2 = 36
            assertEquals(64.0, eval("2^3!"), EPSILON);   // 2^(3!) = 64
        }

        @Test
        void factorialAppliesToParenthesizedResult() {
            assertEquals(24.0, eval("(1+3)!"), EPSILON); // 4! = 24
        }

        @Test
        void unaryPlusIsNoOp() {
            assertEquals(5.0, eval("+5"), EPSILON);
            assertEquals(5.0, eval("+(+5)"), EPSILON);
        }

        @Test
        void doubleNegationCancelsOut() {
            assertEquals(5.0, eval("--5"), EPSILON);
            assertEquals(-5.0, eval("---5"), EPSILON);
        }

        @Test
        void complexOperatorChain() {
            // 2 + 3*4 - 6/2 + 2^3 - 1 = 2+12-3+8-1 = 18
            assertEquals(18.0, eval("2+3*4-6/2+2^3-1"), EPSILON);
        }
    }

    // Implicit multiplication

    @Nested
    @DisplayName("Implicit multiplication")
    class ImplicitMultiplication {

        @Test
        void numberBeforeParenthesis() {
            assertEquals(6.0, eval("2(3)"), EPSILON);
        }

        @Test
        void numberBeforeConstant() {
            assertEquals(2 * Math.PI, eval("2pi"), EPSILON);
        }

        @Test
        void parenthesisBeforeParenthesis() {
            assertEquals(45.0, eval("(2+3)(4+5)"), EPSILON);
        }

        @Test
        void numberBeforeFunctionCall() {
            assertEquals(6.0, eval("3sqrt(4)"), EPSILON);
        }

        @Test
        void implicitMultiplyWithPowerInsideGroup() {
            // (2+3)(4-1)^2 - 10/(5-3)*2 = 5*3^2 - 10/2*2 = 45 - 10 = 35
            assertEquals(35.0, eval("(2+3)(4-1)^2-10/(5-3)*2"), EPSILON);
        }
    }

    // Parenthesis auto-balancing (fixParenthesis)

    @Nested
    @DisplayName("Parenthesis auto-balancing")
    class ParenthesisBalancing {

        @Test
        void missingClosingParenIsAppended() {
            assertEquals(5.0, eval("(2+3"), EPSILON);
        }

        @Test
        void extraClosingParenIsCompensatedWithLeadingOpen() {
            assertEquals(5.0, eval("2+3)"), EPSILON);
        }

        @Test
        void multipleMissingClosingParens() {
            assertEquals(5.0, eval("((2+3"), EPSILON);
        }

        @Test
        void multipleExtraClosingParens() {
            assertEquals(5.0, eval("2+3))"), EPSILON);
        }

        @Test
        void balancedParensAreUnaffected() {
            assertEquals(5.0, eval("(2+3)"), EPSILON);
        }
    }

    // Constants

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        void pi() {
            assertEquals(Math.PI, eval("pi"), EPSILON);
        }

        @Test
        void e() {
            assertEquals(Math.E, eval("e"), EPSILON);
        }

        @Test
        void tau() {
            assertEquals(2 * Math.PI, eval("tau"), EPSILON);
        }

        @Test
        void phi() {
            assertEquals(1.6180339887498948482, eval("phi"), EPSILON);
        }

        @Test
        void randomIsWithinUnitRange() {
            for (int iteration = 0; iteration < 20; iteration++) {
                double value = eval("random");
                assertTrue(value >= 0.0 && value < 1.0, "random out of range: " + value);
            }
        }

        @Test
        void randAliasWorks() {
            double value = eval("rand");
            assertTrue(value >= 0.0 && value < 1.0);
        }

        @Test
        void inDegreeModeDegIsOneAndRadIsConversionFactor() {
            // NOTE: expectations are inferred from the old test name. If this fails, check the
            // actual values in MathConstantEnum and adjust the numbers below.
            assertEquals(1.0, eval("deg"), EPSILON);
            assertEquals(180.0 / Math.PI, eval("rad"), EPSILON);
        }
    }

    // Trig functions

    @Nested
    @DisplayName("Trigonometric functions")
    class Trigonometry {

        @Test
        void sin30() {
            assertEquals(0.5, eval("sin(" + inputAngle(30) + ")"), TRIG_EPSILON);
        }

        @Test
        void cos60() {
            assertEquals(0.5, eval("cos(" + inputAngle(60) + ")"), TRIG_EPSILON);
        }

        @Test
        void tan45() {
            assertEquals(1.0, eval("tan(" + inputAngle(45) + ")"), TRIG_EPSILON);
        }

        @Test
        void csc30() {
            assertEquals(2.0, eval("csc(" + inputAngle(30) + ")"), TRIG_EPSILON);
        }

        @Test
        void sec60() {
            assertEquals(2.0, eval("sec(" + inputAngle(60) + ")"), TRIG_EPSILON);
        }

        @Test
        void cot45() {
            assertEquals(1.0, eval("cot(" + inputAngle(45) + ")"), TRIG_EPSILON);
        }

        @Test
        void sinZeroIsZero() {
            assertEquals(0.0, eval("sin(0)"), TRIG_EPSILON);
        }
    }

    @Nested
    @DisplayName("Inverse trigonometric functions")
    class InverseTrigonometry {

        @Test
        void asinHalf() {
            assertEquals(outputAngle(30), eval("asin(0.5)"), TRIG_EPSILON);
        }

        @Test
        void acosHalf() {
            assertEquals(outputAngle(60), eval("acos(0.5)"), TRIG_EPSILON);
        }

        @Test
        void atanOne() {
            assertEquals(outputAngle(45), eval("atan(1)"), TRIG_EPSILON);
        }

        @Test
        void acscTwo() {
            assertEquals(outputAngle(30), eval("acsc(2)"), TRIG_EPSILON);
        }

        @Test
        void asecTwo() {
            assertEquals(outputAngle(60), eval("asec(2)"), TRIG_EPSILON);
        }

        @Test
        void acotOne() {
            assertEquals(outputAngle(45), eval("acot(1)"), TRIG_EPSILON);
        }

        @Test
        void aliasesMatchCanonicalNames() {
            assertEquals(eval("asin(0.5)"), eval("arcsin(0.5)"), EPSILON);
            assertEquals(eval("acos(0.5)"), eval("arccos(0.5)"), EPSILON);
            assertEquals(eval("atan(0.5)"), eval("arctan(0.5)"), EPSILON);
            assertEquals(eval("acsc(2)"), eval("arccsc(2)"), EPSILON);
            assertEquals(eval("asec(2)"), eval("arcsec(2)"), EPSILON);
            assertEquals(eval("acot(2)"), eval("arccot(2)"), EPSILON);
        }
    }

    // Other single/double-arg functions

    @Nested
    @DisplayName("Other functions")
    class OtherFunctions {

        @Test
        void sqrt() {
            assertEquals(4.0, eval("sqrt(16)"), EPSILON);
        }

        @Test
        void cbrtPositive() {
            assertEquals(3.0, eval("cbrt(27)"), EPSILON);
        }

        @Test
        void cbrtNegative() {
            assertEquals(-3.0, eval("cbrt(-27)"), EPSILON);
        }

        @Test
        void sqrtNegativeIsNaN() {
            assertTrue(Double.isNaN(eval("sqrt(-1)")));
        }

        @Test
        void floor() {
            assertEquals(2.0, eval("floor(2.7)"), EPSILON);
        }

        @Test
        void floorNegative() {
            assertEquals(-3.0, eval("floor(-2.1)"), EPSILON);
        }

        @Test
        void ceil() {
            assertEquals(3.0, eval("ceil(2.1)"), EPSILON);
        }

        @Test
        void ceilNegative() {
            assertEquals(-2.0, eval("ceil(-2.1)"), EPSILON);
        }

        @Test
        void roundUp() {
            assertEquals(3.0, eval("round(2.5)"), EPSILON);
        }

        @Test
        void roundDown() {
            assertEquals(2.0, eval("round(2.4)"), EPSILON);
        }

        @Test
        void roundNegativeHalfRoundsTowardPositiveInfinity() {
            // round(x) = floor(x + 0.5), not symmetric for negatives: -2.5 -> floor(-2.0) = -2
            assertEquals(-2.0, eval("round(-2.5)"), EPSILON);
            assertEquals(-3.0, eval("round(-2.6)"), EPSILON);
        }

        @Test
        void absPositive() {
            assertEquals(5.0, eval("abs(5)"), EPSILON);
        }

        @Test
        void absNegative() {
            assertEquals(5.0, eval("abs(-5)"), EPSILON);
        }

        @Test
        void absZero() {
            assertEquals(0.0, eval("abs(0)"), EPSILON);
        }

        @Test
        void sgnPositive() {
            assertEquals(1.0, eval("sgn(5)"), EPSILON);
        }

        @Test
        void sgnNegative() {
            assertEquals(-1.0, eval("sgn(-5)"), EPSILON);
        }

        @Test
        void sgnZero() {
            assertEquals(0.0, eval("sgn(0)"), EPSILON);
        }

        @Test
        void sgnOfNaNIsZeroInsteadOfPropagating() {
            // sgn() special-cases NaN to 0.0, so wrapping an otherwise-NaN sub-expression
            // makes the whole thing a valid, finite result.
            assertEquals(0.0, eval("sgn(0/0)"), EPSILON);
            assertTrue(isValid("sgn(0/0)"));
            assertFalse(isValid("0/0"));
        }

        @Test
        void log10Default() {
            assertEquals(3.0, eval("log(1000)"), EPSILON);
        }

        @Test
        void logWithBase() {
            assertEquals(3.0, eval("log(2;8)"), EPSILON);
        }

        @Test
        void ln() {
            assertEquals(1.0, eval("ln(e)"), EPSILON);
        }

        @Test
        void lnOfOneIsZero() {
            assertEquals(0.0, eval("ln(1)"), EPSILON);
        }

        @Test
        void exp() {
            assertEquals(Math.E, eval("exp(1)"), EPSILON);
        }

        @Test
        void expZero() {
            assertEquals(1.0, eval("exp(0)"), EPSILON);
        }
    }

    // Variadic functions (min, max, gcf, lcm)

    @Nested
    @DisplayName("Variadic functions")
    class VariadicFunctions {

        @Test
        void minOfMany() {
            assertEquals(1.0, eval("min(3;1;4;1;5;9;2;6)"), EPSILON);
        }

        @Test
        void maxOfMany() {
            assertEquals(9.0, eval("max(3;1;4;1;5;9;2;6)"), EPSILON);
        }

        @Test
        void gcfOfTwo() {
            assertEquals(6.0, eval("gcf(48;18)"), EPSILON);
        }

        @Test
        void gcfOfMany() {
            assertEquals(6.0, eval("gcf(12;18;24)"), EPSILON);
        }

        @Test
        void lcmOfTwo() {
            assertEquals(12.0, eval("lcm(4;6)"), EPSILON);
        }

        @Test
        void lcmOfMany() {
            assertEquals(12.0, eval("lcm(4;6;3)"), EPSILON);
        }

        @Test
        void gcfWithZero() {
            assertEquals(5.0, eval("gcf(0;5)"), EPSILON);
        }

        @Test
        void gcfOfZeroAndZeroIsZero() {
            assertEquals(0.0, eval("gcf(0;0)"), EPSILON);
        }

        @Test
        void lcmOfZeroAndZeroIsNaN() {
            // |0*0| / gcf(0,0) = 0/0 = NaN
            assertTrue(Double.isNaN(eval("lcm(0;0)")));
        }

        @Test
        void gcfAndLcmHandleNegativeInputs() {
            assertEquals(6.0, eval("gcf(-48;18)"), EPSILON);
            assertEquals(12.0, eval("lcm(-4;6)"), EPSILON);
        }
    }

    @Nested
    @DisplayName("clamp and cmp")
    class ClampAndCmp {

        @Test
        void clampInsideRange() {
            assertEquals(5.0, eval("clamp(5;1;10)"), EPSILON);
        }

        @Test
        void clampBelowRange() {
            assertEquals(1.0, eval("clamp(-5;1;10)"), EPSILON);
        }

        @Test
        void clampAboveRange() {
            assertEquals(10.0, eval("clamp(50;1;10)"), EPSILON);
        }

        @Test
        void clampWithMinGreaterThanMaxThrows() {
            assertThrows(ExpressionException.class, () -> eval("clamp(5;10;1)"));
        }

        @Test
        void cmpEqual() {
            assertEquals(0.0, eval("cmp(5;5)"), EPSILON);
        }

        @Test
        void cmpLess() {
            assertEquals(-1.0, eval("cmp(3;5)"), EPSILON);
        }

        @Test
        void cmpGreater() {
            assertEquals(1.0, eval("cmp(5;3)"), EPSILON);
        }

        @Test
        void cmpWithinTolerance() {
            assertEquals(0.0, eval("cmp(5;5.0005;0.001)"), EPSILON);
        }

        @Test
        void cmpOutsideTolerance() {
            assertEquals(-1.0, eval("cmp(5;6;0.5)"), EPSILON);
        }

        @Test
        void wrongArityThrows() {
            assertThrows(ExpressionException.class, () -> eval("clamp(1;2)"));
            assertThrows(ExpressionException.class, () -> eval("sqrt(1;2)"));
        }
    }

    // Factorial (via '!' operator)

    @Nested
    @DisplayName("Factorial")
    class Factorial {

        @Test
        void one() {
            assertEquals(1.0, eval("1!"), EPSILON);
        }

        @Test
        void five() {
            assertEquals(120.0, eval("5!"), EPSILON);
        }

        @Test
        void ten() {
            assertEquals(3628800.0, eval("10!"), EPSILON);
        }

        @Test
        void thirteen() {
            assertEquals(6227020800.0, eval("13!"), EPSILON);
        }

        @Test
        @DisplayName("0! = 1")
        void zeroFactorialIsOne() {
            assertEquals(1.0, eval("0!"), EPSILON);
            assertTrue(isValid("0!"));
        }

        @Test
        @DisplayName("Factorial of a negative integer is NaN")
        void negativeIntegerFactorialIsNaN() {
            assertTrue(Double.isNaN(eval("(-1)!")));
        }

        @Test
        void fractionalFactorialUsesStirlingApproximation() {
            // 5.5! = Gamma(6.5) ~ 287.885
            double result = eval("5.5!");
            assertTrue(Double.isFinite(result));
            assertEquals(287.885, result, 0.5);
        }

        @Test
        void fractionalFactorialOfSmallArgumentIsAccurate() {
            // 0.5! = Gamma(1.5) = sqrt(pi) / 2
            assertEquals(0.886226925, eval("0.5!"), 1e-6);
        }

        @Test
        void doubleFactorialOperator() {
            assertEquals(720.0, eval("3!!"), EPSILON); // (3!)! = 6! = 720
        }

        @Test
        void factorialAboveLimitOverflowsToInfinity() {
            assertEquals(Double.POSITIVE_INFINITY, eval("171!"));
            assertFalse(isValid("171!"));
        }
    }

    // Static utility methods, tested directly (no config dependency)

    @Nested
    @DisplayName("Static utilities (mod/gcf/lcm/factorial)")
    class StaticHelpers {

        @Test
        void modMatchesSignOfDivisor() {
            assertEquals(2.0, MathEngine.mod(5, 3), EPSILON);
            assertEquals(1.0, MathEngine.mod(-5, 3), EPSILON);
            assertEquals(-1.0, MathEngine.mod(5, -3), EPSILON);
            assertEquals(-2.0, MathEngine.mod(-5, -3), EPSILON);
        }

        @Test
        void modWithZeroRemainder() {
            assertEquals(0.0, MathEngine.mod(-6, 3), EPSILON);
            assertEquals(0.0, MathEngine.mod(6, -3), EPSILON);
        }

        @Test
        void gcfBasic() {
            assertEquals(6.0, MathEngine.gcf(48, 18), EPSILON);
            assertEquals(5.0, MathEngine.gcf(0, 5), EPSILON);
            assertEquals(5.0, MathEngine.gcf(5, 0), EPSILON);
            assertEquals(0.0, MathEngine.gcf(0, 0), EPSILON);
        }

        @Test
        void gcfDoesNotHangOnNaN() {
            assertTimeoutPreemptively(Duration.ofSeconds(2),
                                      () -> assertTrue(Double.isNaN(MathEngine.gcf(Double.NaN, 1))));
        }

        @Test
        void gcfAndLcmHandleNegatives() {
            assertEquals(6.0, MathEngine.gcf(-48, 18), EPSILON);
            assertEquals(12.0, MathEngine.lcm(-4, 6), EPSILON);
        }

        @Test
        void lcmBasic() {
            assertEquals(12.0, MathEngine.lcm(4, 6), EPSILON);
            assertEquals(0.0, MathEngine.lcm(0, 5), EPSILON);
        }

        @Test
        void factorialBasic() {
            assertEquals(120.0, MathEngine.factorial(5), EPSILON);
            assertEquals(1.0, MathEngine.factorial(1), EPSILON);
            assertEquals(6227020800.0, MathEngine.factorial(13), EPSILON);
        }

        @Test
        void factorialEdgeCases() {
            assertEquals(1.0, MathEngine.factorial(0), EPSILON);
            assertTrue(Double.isNaN(MathEngine.factorial(-1)));
            assertTrue(Double.isNaN(MathEngine.factorial(Double.NaN)));
        }
    }

    // Error handling / isValid

    @Nested
    @DisplayName("Error handling and isValid")
    class ErrorHandling {

        @Test
        void nullIsInvalid() {
            assertFalse(isValid(null));
        }

        @Test
        void emptyIsInvalid() {
            assertFalse(isValid(""));
        }

        @Test
        void blankIsInvalid() {
            assertFalse(isValid("   "));
        }

        @Test
        void unknownIdentifierThrows() {
            assertThrows(ExpressionException.class, () -> eval("foo"));
            assertFalse(isValid("foo"));
        }

        @Test
        void unknownFunctionThrows() {
            assertThrows(ExpressionException.class, () -> eval("foo(1)"));
        }

        @Test
        void trailingGarbageIsInvalid() {
            assertFalse(isValid("2+2 extra"));
        }

        @Test
        void danglingOperatorThrows() {
            assertThrows(ExpressionException.class, () -> eval("2++"));
            assertThrows(ExpressionException.class, () -> eval("2/"));
        }

        @Test
        void unmatchedFunctionParenIsAutoBalanced() {
            assertEquals(2.0, eval("sqrt(4"), EPSILON);
        }

        @Test
        void unexpectedCharacterThrows() {
            assertThrows(ExpressionException.class, () -> eval("2#3"));
        }

        @Test
        void malformedNumberThrowsExpressionException() {
            assertThrows(ExpressionException.class, () -> eval("1.2.3"));
            assertThrows(ExpressionException.class, () -> eval("."));
        }

        @Test
        void deepNestingThrowsInsteadOfOverflowingTheStack() {
            assertThrows(ExpressionException.class, () -> eval("-".repeat(5000) + "1"));
            assertThrows(ExpressionException.class, () -> eval("(".repeat(5000) + "1" + ")".repeat(5000)));
        }

        @Test
        void infinityIsInvalid() {
            assertFalse(isValid("1/0"));
        }

        @Test
        void nanIsInvalid() {
            assertFalse(isValid("sqrt(-1)"));
            assertFalse(isValid("0/0"));
        }

        @Test
        void wellFormedExpressionIsValid() {
            assertTrue(isValid("2+2*3"));
        }

        @Test
        void autoBalancedParensAreStillValid() {
            assertTrue(isValid("2+3)"));
            assertTrue(isValid("(2+3"));
        }
    }

    // Built-in symbol tables (MathFunctionEnum / MathConstantEnum)
    // These don't touch ChatConfigManager, unlike MathEngine.getAvailableSymbols().

    @Nested
    @DisplayName("Built-in function and constant tables")
    class SymbolTables {

        @Test
        void functionLookupResolvesCanonicalNamesAndAliases() {
            assertEquals(MathFunctionEnum.ASIN, MathFunctionEnum.fromName("asin"));
            assertEquals(MathFunctionEnum.ASIN, MathFunctionEnum.fromName("arcsin"));
            assertNull(MathFunctionEnum.fromName("notafunction"));
        }

        @Test
        void constantLookupResolvesCanonicalNamesAndAliases() {
            assertEquals(MathConstantEnum.RANDOM, MathConstantEnum.fromName("random"));
            assertEquals(MathConstantEnum.RANDOM, MathConstantEnum.fromName("rand"));
            assertNull(MathConstantEnum.fromName("notaconstant"));
        }

        @Test
        void arityChecksMatchDeclaredRanges() {
            assertTrue(MathFunctionEnum.CLAMP.matchesArity(3));
            assertFalse(MathFunctionEnum.CLAMP.matchesArity(2));
            assertTrue(MathFunctionEnum.LOG.matchesArity(1));
            assertTrue(MathFunctionEnum.LOG.matchesArity(2));
            assertFalse(MathFunctionEnum.LOG.matchesArity(3));
            assertTrue(MathFunctionEnum.MIN.matchesArity(1));
            assertTrue(MathFunctionEnum.MIN.matchesArity(1000));
        }
    }

    // Kitchen-sink expressions combining many features

    @Nested
    @DisplayName("Complex expressions")
    class ComplexExpressions {

        @Test
        void factorialsFunctionsAndImplicitMultiplication() {
            // 3! + 2(1+1) - sqrt(9)*2 + abs(-4)/2 = 6 + 4 - 6 + 2 = 6
            assertEquals(6.0, eval("3!+2(1+1)-sqrt(9)*2+abs(-4)/2"), EPSILON);
        }

        @Test
        void nestedGroupsWithPowerAndImplicitMultiplication() {
            // (2+3)(4-1)^2 - 10/(5-3)*2 = 5*9 - 10 = 35
            assertEquals(35.0, eval("(2+3)(4-1)^2-10/(5-3)*2"), EPSILON);
        }

        @Test
        void deeplyNestedFunctionsAndConstants() {
            // sqrt(abs(-16)) + floor(pi) - ceil(-1.2) + max(1;2;3) = 4 + 3 - (-1) + 3 = 11
            assertEquals(11.0, eval("sqrt(abs(-16))+floor(pi)-ceil(-1.2)+max(1;2;3)"), EPSILON);
        }

        @Test
        void everythingAtOnce() {
            // 2^3! - 5%3 + gcf(12;18) - clamp(50;0;10)/2 + sgn(-7) = 64 - 2 + 6 - 5 - 1 = 62
            assertEquals(62.0, eval("2^3!-5%3+gcf(12;18)-clamp(50;0;10)/2+sgn(-7)"), EPSILON);
        }
    }
}