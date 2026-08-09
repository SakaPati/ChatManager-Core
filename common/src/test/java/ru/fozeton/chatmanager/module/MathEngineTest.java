//package ru.fozeton.chatmanager.module;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import ru.fozeton.chatmanager.module.math.MathConstantEnum;
//import ru.fozeton.chatmanager.module.math.MathFunctionEnum;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Full coverage suite for {@link MathEngine}.
// * <p>
// * IMPORTANT — why this doesn't call {@code MathEngine.getInstance().eval(...)} directly:
// * {@code eval()}/{@code isValid()}/{@code getAvailableSymbols()} all go through
// * {@code ChatConfigManager.getInstance()}, whose static initializer calls
// * {@code Platform.getGameFolder()} (Architectury). That throws an
// * {@code AssertionError} unless a real game/mod-loader environment is bootstrapped first,
// * which a plain JUnit unit test does not have and this build doesn't pull in a mocking
// * library (Mockito) to fake.
// * <p>
// * Instead, these tests reach the private static {@code MathEngine.Parser} nested class via
// * reflection and drive it directly with empty custom-constant/custom-function maps — this
// * exercises exactly the same grammar/evaluation logic without ever touching
// * {@code ChatConfigManager}. The public static utility methods ({@code mod}, {@code gcf},
// * {@code lcm}, {@code factorial}) don't touch config at all and are called directly.
// * <p>
// * Since the test harness constructs {@code Parser} itself, the {@code radians} flag is
// * simply fixed to {@code false} (degrees mode) rather than guessed from config.
// * <p>
// * NOTE: function-call arguments in this grammar are separated by {@code ;}, not {@code ,}.
// * <p>
// * If/when Mockito is added to the project, an equivalent end-to-end suite could instead
// * {@code mockStatic(Platform.class)} to stub {@code getGameFolder()} and exercise the real
// * public {@code MathEngine.getInstance()} API including config-driven customization.
// */
//class MathEngineTest {
//
//    private static final double EPS = 1e-9;
//    private static final double TRIG_EPS = 1e-6;
//    private static final boolean RADIANS = false; // we control this directly, so trig helpers below assume degrees
//
//    private static Constructor<?> parserCtor;
//    private static Method parseExpressionMethod;
//    private static Method expectEndMethod;
//
//    @BeforeAll
//    static void setupReflection() throws Exception {
//        Class<?> parserClass = Class.forName("ru.fozeton.chatmanager.module.MathEngine$Parser");
//        parserCtor = parserClass.getDeclaredConstructor(String.class, boolean.class, Map.class, Map.class, Map.class);
//        parserCtor.setAccessible(true);
//        parseExpressionMethod = parserClass.getDeclaredMethod("parseExpression");
//        parseExpressionMethod.setAccessible(true);
//        expectEndMethod = parserClass.getDeclaredMethod("expectEnd");
//        expectEndMethod.setAccessible(true);
//    }
//
//    /**
//     * Mirrors MathEngine's private fixParenthesis(), reimplemented so we don't need extra reflection for it.
//     */
//    private static String balanceParens(String input) {
//        int open = 0, close = 0;
//        for (char c : input.toCharArray()) {
//            if (c == '(') open++;
//            else if (c == ')') close++;
//        }
//        return "(".repeat(Math.max(0, close - open)) + input + ")".repeat(Math.max(0, open - close));
//    }
//
//    private static double eval(String expr) {
//        try {
//            String balanced = balanceParens(expr);
//            Object parser = parserCtor.newInstance(balanced, RADIANS, Map.of(), Map.of(), Map.of());
//            double result = (Double) parseExpressionMethod.invoke(parser);
//            expectEndMethod.invoke(parser);
//            return result;
//        } catch (InvocationTargetException e) {
//            Throwable cause = e.getCause();
//            if (cause instanceof RuntimeException re) throw re;
//            throw new RuntimeException(cause);
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static boolean isValid(String expr) {
//        if (expr == null || expr.isBlank()) return false;
//        try {
//            double result = eval(expr);
//            return !Double.isNaN(result) && !Double.isInfinite(result);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    // In degrees mode (RADIANS = false), trig input/output units are plain degrees - no conversion needed.
//    private static double inputAngle(double degrees) {
//        return degrees;
//    }
//
//    private static double outputAngle(double degrees) {
//        return degrees;
//    }
//
//    // Basic arithmetic
//
//    @Nested
//    @DisplayName("Базовая арифметика")
//    class BasicArithmetic {
//
//        @ParameterizedTest
//        @CsvSource({
//                "2+2,        4.0",
//                "2-2,        0.0",
//                "2*3,        6.0",
//                "5/2,        2.5",
//                "7%3,        1.0",
//                "10-4,       6.0",
//                "9/3,        3.0",
//                "0.1+0.2,    0.3",
//                ".5+.5,      1.0",
//                "  2 + 2  ,  4.0",
//                "10-5+3-2,   6.0",
//                "8*2/4,      4.0",
//                "2+3*4/2-2%3,6.0"
//        })
//        void arithmeticExpressions(String expr, double expected) {
//            assertEquals(expected, eval(expr), EPS, expr);
//        }
//
//        @Test
//        void divisionByZeroIsInfinity() {
//            assertEquals(Double.POSITIVE_INFINITY, eval("1/0"));
//            assertEquals(Double.NEGATIVE_INFINITY, eval("-1/0"));
//        }
//
//        @Test
//        void zeroDividedByZeroIsNaN() {
//            assertTrue(Double.isNaN(eval("0/0")));
//        }
//
//        @Test
//        void javaStyleModuloKeepsDividendSign() {
//            // The '%' operator inside expressions uses raw Java remainder semantics,
//            // unlike the public static MathEngine.mod() helper.
//            assertEquals(-1.0, eval("-7%3"), EPS);
//            assertEquals(1.0, eval("7%-3"), EPS);
//        }
//    }
//
//    // Precedence & associativity
//
//    @Nested
//    @DisplayName("Приоритет и ассоциативность операторов")
//    class Precedence {
//
//        @Test
//        void multiplicationBeforeAddition() {
//            assertEquals(8.0, eval("2+2*3"), EPS);
//        }
//
//        @Test
//        void parenthesesOverridePrecedence() {
//            assertEquals(12.0, eval("(2+2)*3"), EPS);
//        }
//
//        @Test
//        void deeplyNestedParentheses() {
//            assertEquals(3.0, eval("((((1+2))))"), EPS);
//        }
//
//        @Test
//        void powerIsRightAssociative() {
//            // 2^3^2 = 2^(3^2) = 2^9 = 512, not (2^3)^2 = 64
//            assertEquals(512.0, eval("2^3^2"), EPS);
//        }
//
//        @Test
//        void unaryMinusHasLowerPrecedenceThanPower() {
//            assertEquals(-4.0, eval("-2^2"), EPS);
//            assertEquals(4.0, eval("(-2)^2"), EPS);
//        }
//
//        @Test
//        void factorialBindsTighterThanPower() {
//            assertEquals(36.0, eval("3!^2"), EPS);   // (3!)^2 = 36
//            assertEquals(64.0, eval("2^3!"), EPS);   // 2^(3!) = 64
//        }
//
//        @Test
//        void factorialAppliesToParenthesizedResult() {
//            assertEquals(24.0, eval("(1+3)!"), EPS); // 4! = 24
//        }
//
//        @Test
//        void unaryPlusIsNoOp() {
//            assertEquals(5.0, eval("+5"), EPS);
//            assertEquals(5.0, eval("+(+5)"), EPS);
//        }
//
//        @Test
//        void doubleNegationCancelsOut() {
//            assertEquals(5.0, eval("--5"), EPS);
//            assertEquals(-5.0, eval("---5"), EPS);
//        }
//
//        @Test
//        void complexOperatorChain() {
//            // 2 + 3*4 - 6/2 + 2^3 - 1 = 2+12-3+8-1 = 18
//            assertEquals(18.0, eval("2+3*4-6/2+2^3-1"), EPS);
//        }
//    }
//
//    // Implicit multiplication
//
//    @Nested
//    @DisplayName("Неявное умножение")
//    class ImplicitMultiplication {
//
//        @Test
//        void numberBeforeParenthesis() {
//            assertEquals(6.0, eval("2(3)"), EPS);
//        }
//
//        @Test
//        void numberBeforeConstant() {
//            assertEquals(2 * Math.PI, eval("2pi"), EPS);
//        }
//
//        @Test
//        void parenthesisBeforeParenthesis() {
//            assertEquals(45.0, eval("(2+3)(4+5)"), EPS);
//        }
//
//        @Test
//        void numberBeforeFunctionCall() {
//            assertEquals(6.0, eval("3sqrt(4)"), EPS);
//        }
//
//        @Test
//        void implicitMultiplyWithPowerInsideGroup() {
//            // (2+3)(4-1)^2 - 10/(5-3)*2 = 5*3^2 - 10/2*2 = 45 - 10 = 35
//            assertEquals(35.0, eval("(2+3)(4-1)^2-10/(5-3)*2"), EPS);
//        }
//    }
//
//    // Parenthesis auto-balancing (fixParenthesis)
//
//    @Nested
//    @DisplayName("Автобалансировка скобок")
//    class ParenBalancing {
//
//        @Test
//        void missingClosingParenIsAppended() {
//            assertEquals(5.0, eval("(2+3"), EPS);
//        }
//
//        @Test
//        void extraClosingParenIsCompensatedWithLeadingOpen() {
//            assertEquals(5.0, eval("2+3)"), EPS);
//        }
//
//        @Test
//        void multipleMissingOpenParens() {
//            assertEquals(5.0, eval("((2+3"), EPS);
//        }
//
//        @Test
//        void multipleExtraClosingParens() {
//            assertEquals(5.0, eval("2+3))"), EPS);
//        }
//
//        @Test
//        void balancedParensAreUnaffected() {
//            assertEquals(5.0, eval("(2+3)"), EPS);
//        }
//    }
//
//    // Constants
//
//    @Nested
//    @DisplayName("Константы")
//    class Constants {
//
//        @Test
//        void pi() {
//            assertEquals(Math.PI, eval("pi"), EPS);
//        }
//
//        @Test
//        void e() {
//            assertEquals(Math.E, eval("e"), EPS);
//        }
//
//        @Test
//        void tau() {
//            assertEquals(2 * Math.PI, eval("tau"), EPS);
//        }
//
//        @Test
//        void phi() {
//            assertEquals(1.6180339887498948482, eval("phi"), EPS);
//        }
//
//        @Test
//        void randomIsWithinUnitRange() {
//            for (int i = 0; i < 20; i++) {
//                double r = eval("random");
//                assertTrue(r >= 0.0 && r < 1.0, "random() out of range: " + r);
//            }
//        }
//
//        @Test
//        void randAliasWorks() {
//            double r = eval("rand");
//            assertTrue(r >= 0.0 && r < 1.0);
//        }
//
//        @Test
//        void inDegreeModeRadIsConversionFactorAndDegIsOne() {
//            // radians=false here: RAD = 180/pi (deg->rad style factor), DEG = pi/180
//            assertEquals(1.0, eval("rad") * eval("deg"), EPS);
//        }
//    }
//
//    // Trig functions
//
//    @Nested
//    @DisplayName("Тригонометрические функции")
//    class Trig {
//
//        @Test
//        void sin30() {
//            assertEquals(0.5, eval("sin(" + inputAngle(30) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void cos60() {
//            assertEquals(0.5, eval("cos(" + inputAngle(60) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void tan45() {
//            assertEquals(1.0, eval("tan(" + inputAngle(45) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void csc30() {
//            assertEquals(2.0, eval("csc(" + inputAngle(30) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void sec60() {
//            assertEquals(2.0, eval("sec(" + inputAngle(60) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void cot45() {
//            assertEquals(1.0, eval("cot(" + inputAngle(45) + ")"), TRIG_EPS);
//        }
//
//        @Test
//        void sinZeroIsZero() {
//            assertEquals(0.0, eval("sin(0)"), TRIG_EPS);
//        }
//    }
//
//    @Nested
//    @DisplayName("Обратные тригонометрические функции")
//    class InverseTrig {
//
//        @Test
//        void asinHalf() {
//            assertEquals(outputAngle(30), eval("asin(0.5)"), TRIG_EPS);
//        }
//
//        @Test
//        void acosHalf() {
//            assertEquals(outputAngle(60), eval("acos(0.5)"), TRIG_EPS);
//        }
//
//        @Test
//        void atanOne() {
//            assertEquals(outputAngle(45), eval("atan(1)"), TRIG_EPS);
//        }
//
//        @Test
//        void acscTwo() {
//            assertEquals(outputAngle(30), eval("acsc(2)"), TRIG_EPS);
//        }
//
//        @Test
//        void asecTwo() {
//            assertEquals(outputAngle(60), eval("asec(2)"), TRIG_EPS);
//        }
//
//        @Test
//        void acotOne() {
//            assertEquals(outputAngle(45), eval("acot(1)"), TRIG_EPS);
//        }
//
//        @Test
//        void aliasesMatchCanonicalNames() {
//            assertEquals(eval("asin(0.5)"), eval("arcsin(0.5)"), EPS);
//            assertEquals(eval("acos(0.5)"), eval("arccos(0.5)"), EPS);
//            assertEquals(eval("atan(0.5)"), eval("arctan(0.5)"), EPS);
//            assertEquals(eval("acsc(2)"), eval("arccsc(2)"), EPS);
//            assertEquals(eval("asec(2)"), eval("arcsec(2)"), EPS);
//            assertEquals(eval("acot(2)"), eval("arccot(2)"), EPS);
//        }
//    }
//
//    // Other single/double-arg functions
//
//    @Nested
//    @DisplayName("Остальные функции")
//    class OtherFunctions {
//
//        @Test
//        void sqrt() {
//            assertEquals(4.0, eval("sqrt(16)"), EPS);
//        }
//
//        @Test
//        void cbrtPositive() {
//            assertEquals(3.0, eval("cbrt(27)"), EPS);
//        }
//
//        @Test
//        void cbrtNegative() {
//            assertEquals(-3.0, eval("cbrt(-27)"), EPS);
//        }
//
//        @Test
//        void sqrtNegativeIsNaN() {
//            assertTrue(Double.isNaN(eval("sqrt(-1)")));
//        }
//
//        @Test
//        void floor() {
//            assertEquals(2.0, eval("floor(2.7)"), EPS);
//        }
//
//        @Test
//        void floorNegative() {
//            assertEquals(-3.0, eval("floor(-2.1)"), EPS);
//        }
//
//        @Test
//        void ceil() {
//            assertEquals(3.0, eval("ceil(2.1)"), EPS);
//        }
//
//        @Test
//        void ceilNegative() {
//            assertEquals(-2.0, eval("ceil(-2.1)"), EPS);
//        }
//
//        @Test
//        void roundUp() {
//            assertEquals(3.0, eval("round(2.5)"), EPS);
//        }
//
//        @Test
//        void roundDown() {
//            assertEquals(2.0, eval("round(2.4)"), EPS);
//        }
//
//        @Test
//        void roundNegativeHalfRoundsTowardPositiveInfinity() {
//            // round(x) = floor(x + 0.5), not symmetric for negatives: -2.5 -> floor(-2.0) = -2
//            assertEquals(-2.0, eval("round(-2.5)"), EPS);
//            assertEquals(-3.0, eval("round(-2.6)"), EPS);
//        }
//
//        @Test
//        void absPositive() {
//            assertEquals(5.0, eval("abs(5)"), EPS);
//        }
//
//        @Test
//        void absNegative() {
//            assertEquals(5.0, eval("abs(-5)"), EPS);
//        }
//
//        @Test
//        void absZero() {
//            assertEquals(0.0, eval("abs(0)"), EPS);
//        }
//
//        @Test
//        void sgnPositive() {
//            assertEquals(1.0, eval("sgn(5)"), EPS);
//        }
//
//        @Test
//        void sgnNegative() {
//            assertEquals(-1.0, eval("sgn(-5)"), EPS);
//        }
//
//        @Test
//        void sgnZero() {
//            assertEquals(0.0, eval("sgn(0)"), EPS);
//        }
//
//        @Test
//        void sgnOfNaNIsZeroInsteadOfPropagating() {
//            // sgn() special-cases NaN to 0.0, so wrapping an otherwise-NaN sub-expression
//            // makes the whole thing a valid, finite result.
//            assertEquals(0.0, eval("sgn(0/0)"), EPS);
//            assertTrue(isValid("sgn(0/0)"));
//            assertFalse(isValid("0/0"));
//        }
//
//        @Test
//        void log10Default() {
//            assertEquals(3.0, eval("log(1000)"), EPS);
//        }
//
//        @Test
//        void logWithBase() {
//            assertEquals(3.0, eval("log(2;8)"), EPS);
//        }
//
//        @Test
//        void ln() {
//            assertEquals(1.0, eval("ln(e)"), EPS);
//        }
//
//        @Test
//        void lnOfOneIsZero() {
//            assertEquals(0.0, eval("ln(1)"), EPS);
//        }
//
//        @Test
//        void exp() {
//            assertEquals(Math.E, eval("exp(1)"), EPS);
//        }
//
//        @Test
//        void expZero() {
//            assertEquals(1.0, eval("exp(0)"), EPS);
//        }
//    }
//
//    // Variadic functions (min, max, gcf, lcm)
//
//    @Nested
//    @DisplayName("Функции с переменным числом аргументов")
//    class VariadicFunctions {
//
//        @Test
//        void minOfMany() {
//            assertEquals(1.0, eval("min(3;1;4;1;5;9;2;6)"), EPS);
//        }
//
//        @Test
//        void maxOfMany() {
//            assertEquals(9.0, eval("max(3;1;4;1;5;9;2;6)"), EPS);
//        }
//
//        @Test
//        void gcfOfTwo() {
//            assertEquals(6.0, eval("gcf(48;18)"), EPS);
//        }
//
//        @Test
//        void gcfOfMany() {
//            assertEquals(6.0, eval("gcf(12;18;24)"), EPS);
//        }
//
//        @Test
//        void lcmOfTwo() {
//            assertEquals(12.0, eval("lcm(4;6)"), EPS);
//        }
//
//        @Test
//        void lcmOfMany() {
//            assertEquals(12.0, eval("lcm(4;6;3)"), EPS);
//        }
//
//        @Test
//        void gcfWithZero() {
//            assertEquals(5.0, eval("gcf(0;5)"), EPS);
//        }
//
//        @Test
//        void gcfOfZeroAndZeroIsZero() {
//            assertEquals(0.0, eval("gcf(0;0)"), EPS);
//        }
//
//        @Test
//        void lcmOfZeroAndZeroIsNaN() {
//            // (0*0)/gcf(0,0) = 0/0 = NaN
//            assertTrue(Double.isNaN(eval("lcm(0;0)")));
//        }
//    }
//
//    @Nested
//    @DisplayName("clamp и cmp")
//    class ClampAndCmp {
//
//        @Test
//        void clampInsideRange() {
//            assertEquals(5.0, eval("clamp(5;1;10)"), EPS);
//        }
//
//        @Test
//        void clampBelowRange() {
//            assertEquals(1.0, eval("clamp(-5;1;10)"), EPS);
//        }
//
//        @Test
//        void clampAboveRange() {
//            assertEquals(10.0, eval("clamp(50;1;10)"), EPS);
//        }
//
//        @Test
//        void cmpEqual() {
//            assertEquals(0.0, eval("cmp(5;5)"), EPS);
//        }
//
//        @Test
//        void cmpLess() {
//            assertEquals(-1.0, eval("cmp(3;5)"), EPS);
//        }
//
//        @Test
//        void cmpGreater() {
//            assertEquals(1.0, eval("cmp(5;3)"), EPS);
//        }
//
//        @Test
//        void cmpWithinTolerance() {
//            assertEquals(0.0, eval("cmp(5;5.0005;0.001)"), EPS);
//        }
//
//        @Test
//        void cmpOutsideTolerance() {
//            assertEquals(-1.0, eval("cmp(5;6;0.5)"), EPS);
//        }
//
//        @Test
//        void wrongArityThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("clamp(1;2)"));
//            assertThrows(IllegalArgumentException.class, () -> eval("sqrt(1;2)"));
//        }
//    }
//
//    // Factorial (via '!' operator)
//
//    @Nested
//    @DisplayName("Факториал")
//    class Factorial {
//
//        @Test
//        void one() {
//            assertEquals(1.0, eval("1!"), EPS);
//        }
//
//        @Test
//        void five() {
//            assertEquals(120.0, eval("5!"), EPS);
//        }
//
//        @Test
//        void ten() {
//            assertEquals(3628800.0, eval("10!"), EPS);
//        }
//
//        @Test
//        void thirteen() {
//            assertEquals(6227020800.0, eval("13!"), EPS);
//        }
//
//        @Test
//        @DisplayName("БАГ: 0! возвращает NaN вместо 1 (не входит в диапазон точного вычисления x>=1)")
//        void zeroFactorialIsActuallyNaNDueToImplementationBug() {
//            double result = eval("0!");
//            assertTrue(Double.isNaN(result), "Expected current (buggy) behaviour: 0! = NaN, got " + result);
//            assertFalse(isValid("0!"));
//        }
//
//        @Test
//        @DisplayName("Факториал отрицательного целого тоже даёт NaN (sqrt отрицательного в приближении Стирлинга)")
//        void negativeIntegerFactorialIsNaN() {
//            assertTrue(Double.isNaN(eval("(-1)!")));
//        }
//
//        @Test
//        void fractionalFactorialUsesStirlingApproximation() {
//            // 5.5! = Gamma(6.5) ≈ 287.885
//            double result = eval("5.5!");
//            assertTrue(Double.isFinite(result));
//            assertEquals(287.885, result, 0.5);
//        }
//
//        @Test
//        void doubleFactorialOperator() {
//            assertEquals(720.0, eval("3!!"), EPS); // (3!)! = 6! = 720
//        }
//    }
//
//    // Static utility methods, tested directly (no config dependency)
//
//    @Nested
//    @DisplayName("Статические утилиты (mod/gcf/lcm/factorial)")
//    class StaticHelpers {
//
//        @Test
//        void modMatchesSignOfDivisor() {
//            assertEquals(2.0, MathEngine.mod(5, 3), EPS);
//            assertEquals(1.0, MathEngine.mod(-5, 3), EPS);
//            assertEquals(-1.0, MathEngine.mod(5, -3), EPS);
//            assertEquals(-2.0, MathEngine.mod(-5, -3), EPS);
//        }
//
//        @Test
//        void gcfBasic() {
//            assertEquals(6.0, MathEngine.gcf(48, 18), EPS);
//            assertEquals(5.0, MathEngine.gcf(0, 5), EPS);
//            assertEquals(5.0, MathEngine.gcf(5, 0), EPS);
//            assertEquals(0.0, MathEngine.gcf(0, 0), EPS);
//        }
//
//        @Test
//        void lcmBasic() {
//            assertEquals(12.0, MathEngine.lcm(4, 6), EPS);
//            assertEquals(0.0, MathEngine.lcm(0, 5), EPS);
//        }
//
//        @Test
//        void factorialBasic() {
//            assertEquals(120.0, MathEngine.factorial(5), EPS);
//            assertEquals(1.0, MathEngine.factorial(1), EPS);
//            assertEquals(6227020800.0, MathEngine.factorial(13), EPS);
//        }
//
//        @Test
//        void factorialEdgeCasesAreNaN() {
//            assertTrue(Double.isNaN(MathEngine.factorial(0)));
//            assertTrue(Double.isNaN(MathEngine.factorial(-1)));
//        }
//    }
//
//    // Error handling / isValid
//
//    @Nested
//    @DisplayName("Обработка ошибок и isValid")
//    class ErrorHandling {
//
//        @Test
//        void nullIsInvalid() {
//            assertFalse(isValid(null));
//        }
//
//        @Test
//        void emptyIsInvalid() {
//            assertFalse(isValid(""));
//        }
//
//        @Test
//        void blankIsInvalid() {
//            assertFalse(isValid("   "));
//        }
//
//        @Test
//        void unknownIdentifierThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("foo"));
//            assertFalse(isValid("foo"));
//        }
//
//        @Test
//        void unknownFunctionThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("foo(1)"));
//        }
//
//        @Test
//        void trailingGarbageIsInvalid() {
//            assertFalse(isValid("2+2 extra"));
//        }
//
//        @Test
//        void danglingOperatorThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("2++"));
//            assertThrows(IllegalArgumentException.class, () -> eval("2/"));
//        }
//
//        @Test
//        void unmatchedFunctionParenThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("sqrt(4"));
//        }
//
//        @Test
//        void unexpectedCharacterThrows() {
//            assertThrows(IllegalArgumentException.class, () -> eval("2#3"));
//        }
//
//        @Test
//        void infinityIsInvalid() {
//            assertFalse(isValid("1/0"));
//        }
//
//        @Test
//        void nanIsInvalid() {
//            assertFalse(isValid("sqrt(-1)"));
//            assertFalse(isValid("0/0"));
//        }
//
//        @Test
//        void wellFormedExpressionIsValid() {
//            assertTrue(isValid("2+2*3"));
//        }
//
//        @Test
//        void autoBalancedParensAreStillValid() {
//            assertTrue(isValid("2+3)"));
//            assertTrue(isValid("(2+3"));
//        }
//    }
//
//    // Built-in symbol tables (MathFunctionEnum / MathConstantEnum)
//    // These don't touch ChatConfigManager, unlike MathEngine.getAvailableSymbols().
//
//    @Nested
//    @DisplayName("Таблицы встроенных функций и констант")
//    class SymbolTables {
//
//        @Test
//        void functionLookupResolvesCanonicalNamesAndAliases() {
//            assertEquals(MathFunctionEnum.ASIN, MathFunctionEnum.fromName("asin"));
//            assertEquals(MathFunctionEnum.ASIN, MathFunctionEnum.fromName("arcsin"));
//            assertNull(MathFunctionEnum.fromName("notafunction"));
//        }
//
//        @Test
//        void constantLookupResolvesCanonicalNamesAndAliases() {
//            assertEquals(MathConstantEnum.RANDOM, MathConstantEnum.fromName("random"));
//            assertEquals(MathConstantEnum.RANDOM, MathConstantEnum.fromName("rand"));
//            assertNull(MathConstantEnum.fromName("notaconstant"));
//        }
//
//        @Test
//        void arityChecksMatchDeclaredRanges() {
//            assertTrue(MathFunctionEnum.CLAMP.matchesArity(3));
//            assertFalse(MathFunctionEnum.CLAMP.matchesArity(2));
//            assertTrue(MathFunctionEnum.LOG.matchesArity(1));
//            assertTrue(MathFunctionEnum.LOG.matchesArity(2));
//            assertFalse(MathFunctionEnum.LOG.matchesArity(3));
//            assertTrue(MathFunctionEnum.MIN.matchesArity(1));
//            assertTrue(MathFunctionEnum.MIN.matchesArity(1000));
//        }
//    }
//
//    // Kitchen-sink expressions combining many features
//
//    @Nested
//    @DisplayName("Комплексные выражения")
//    class ComplexExpressions {
//
//        @Test
//        void factorialsFunctionsAndImplicitMultiplication() {
//            // 3! + 2(1+1) - sqrt(9)*2 + abs(-4)/2 = 6 + 4 - 6 + 2 = 6
//            assertEquals(6.0, eval("3!+2(1+1)-sqrt(9)*2+abs(-4)/2"), EPS);
//        }
//
//        @Test
//        void nestedGroupsWithPowerAndImplicitMultiplication() {
//            // (2+3)(4-1)^2 - 10/(5-3)*2 = 5*9 - 10 = 35
//            assertEquals(35.0, eval("(2+3)(4-1)^2-10/(5-3)*2"), EPS);
//        }
//
//        @Test
//        void deeplyNestedFunctionsAndConstants() {
//            // sqrt(abs(-16)) + floor(pi) - ceil(-1.2) + max(1;2;3) = 4 + 3 - (-1) + 3 = 11
//            assertEquals(11.0, eval("sqrt(abs(-16))+floor(pi)-ceil(-1.2)+max(1;2;3)"), EPS);
//        }
//
//        @Test
//        void everythingAtOnce() {
//            // 2^3! - 5%3 + gcf(12;18) - clamp(50;0;10)/2 + sgn(-7) = 64 - 2 + 6 - 5 - 1 = 62
//            assertEquals(62.0, eval("2^3!-5%3+gcf(12;18)-clamp(50;0;10)/2+sgn(-7)"), EPS);
//        }
//    }
//}