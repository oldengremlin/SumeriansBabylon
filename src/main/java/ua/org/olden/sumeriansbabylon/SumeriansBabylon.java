package ua.org.olden.sumeriansbabylon;

import java.math.BigInteger;

/**
 * Демонстрація можливостей бібліотеки Base60.
 */
public class SumeriansBabylon {

    public static void main(String[] args) {
        demoBasic();
        System.out.println();
        demoSignNegateAbs();
        System.out.println();
        demoPow();
        System.out.println();
        demoMod();
        System.out.println();
        demoSqrt();
        System.out.println();
        demoNumber();
    }

    // --- Базові операції (з попередньої версії) ---
    static void demoBasic() {
        System.out.println("=== Базові операції ===");
        Base60 a = Base60.parse("2:46:58.30:15");
        System.out.printf("  parse(\"2:46:58.30:15\") → %s → %s%n", a, a.toDecimal());
        System.out.printf("  куніформ: %s%n", a.toSumerianString());

        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(7));
        System.out.printf("  1/7 = %s → %s%n", b.toBase60WithPeriod(), b.toDecimal());

        Base60 c = Base60.parse("1:30");
        Base60 d = Base60.parse("2:15");
        System.out.printf("  %s + %s = %s%n", c, d, c.add(d));
        System.out.printf("  %s × %s = %s%n", c, d, c.multiply(d));
        System.out.printf("  %s ÷ %s = %s%n", c, d, c.divide(d));
    }

    // --- negate / abs / signum ---
    static void demoSignNegateAbs() {
        System.out.println("=== negate / abs / signum ===");
        Base60 x = Base60.parse("3:45.30");
        System.out.printf("  x        = %s%n", x);
        System.out.printf("  negate() = %s%n", x.negate());
        System.out.printf("  abs(-x)  = %s%n", x.negate().abs());
        System.out.printf("  signum() = %d  (знак: -1/0/+1)%n", x.signum());
        System.out.printf("  signum(-x)= %d%n", x.negate().signum());
        System.out.printf("  куніформ -x: %s%n", x.negate().toSumerianString());
    }

    // --- pow ---
    static void demoPow() {
        System.out.println("=== pow ===");
        Base60 two = Base60.fromInt(2);
        System.out.printf("  2^10       = %s%n", two.pow(10));
        System.out.printf("  2^-1       = %s  (1/2)%n", two.pow(-1));
        System.out.printf("  (1:30)^2   = %s%n", Base60.parse("1:30").pow(2));

        // pow(Base60) — цілий показник
        Base60 three = Base60.fromInt(3);
        System.out.printf("  3^Base60(3) = %s%n", three.pow(Base60.fromInt(3)));

        // pow(Base60) — дробовий показник (4^(1/2) = 2)
        Base60 four = Base60.fromInt(4);
        Base60 half = Base60.fromFraction(1, 2);
        System.out.printf("  4^(1/2)    = %s  (через double)%n", four.pow(half));

        // куніформ 60^2
        Base60 sixty = Base60.fromInt(60);
        System.out.printf("  60^2       = %s → %s%n", sixty.pow(2), sixty.pow(2).toSumerianString());
    }

    // --- mod ---
    static void demoMod() {
        System.out.println("=== mod (floor mod) ===");
        Base60 a = Base60.fromInt(7);
        Base60 b = Base60.fromInt(3);
        System.out.printf("   7 mod  3 = %s%n",  a.mod(b));
        System.out.printf("  -7 mod  3 = %s%n",  a.negate().mod(b));
        System.out.printf("   7 mod -3 = %s%n",  a.mod(b.negate()));
        System.out.printf("  -7 mod -3 = %s%n",  a.negate().mod(b.negate()));

        // кутовий приклад — переведення секунд у хвилини:секунди
        Base60 seconds = Base60.fromInt(3723);
        Base60 sixty   = Base60.fromInt(60);
        Base60 minutes = Base60.fromInteger(seconds.toInteger().divide(BigInteger.valueOf(60)));
        Base60 secs    = seconds.mod(sixty);
        System.out.printf("  3723 сек = %s хв %s с%n", minutes, secs);
    }

    // --- sqrt / sqrtSumerians ---
    static void demoSqrt() {
        System.out.println("=== sqrt / sqrtSumerians ===");

        // sqrt через BigDecimal (висока точність)
        Base60 two = Base60.fromInt(2);
        Base60 sqrtClassic = two.sqrt();
        System.out.printf("  √2 (класичний):   %s%n", sqrtClassic.toString(8));
        System.out.printf("  √2 куніформ:      %s%n", sqrtClassic.toSumerianString());

        // sqrtSumerians — метод Герона
        Base60 sqrtBabylon = two.sqrtSumerians();
        System.out.printf("  √2 (вавілонський): %s%n", sqrtBabylon.toString(8));

        // різниця між двома методами
        Base60 diff = sqrtClassic.subtract(sqrtBabylon).abs();
        System.out.printf("  Різниця:          %s%n", diff.toDecimal().toPlainString());

        // Перевірка: sqrt(60^2) = 60
        Base60 sq = Base60.fromInt(3600);
        System.out.printf("  √3600 = %s  (= %s)%n", sq.sqrt(), sq.sqrtSumerians());

        // Вавілонська задача: √2 на табличці YBC 7289
        // Вавілоняни записали 1;24,51,10 ≈ √2
        Base60 ybc = Base60.parse("1.24:51:10");
        System.out.printf("  YBC 7289 (1;24,51,10) = %s%n", ybc.toDecimal());
        System.out.printf("  Наш sqrt(2)           = %s%n", sqrtClassic.toDecimal());
    }

    // --- java.lang.Number ---
    static void demoNumber() {
        System.out.println("=== Base60 як java.lang.Number ===");
        Number n = Base60.fromFraction(7, 2);  // 3.5
        System.out.printf("  7/2 як Number: intValue=%d, longValue=%d, floatValue=%.2f, doubleValue=%.6f%n",
                n.intValue(), n.longValue(), n.floatValue(), n.doubleValue());

        // Використання у Stream через doubleValue
        double sum = java.util.stream.Stream.of(
                Base60.fromInt(1), Base60.fromFraction(1, 2), Base60.fromFraction(1, 3)
        ).mapToDouble(Number::doubleValue).sum();
        System.out.printf("  1 + 1/2 + 1/3 (через doubleValue) ≈ %.6f%n", sum);
        System.out.printf("  1 + 1/2 + 1/3 (точно)             = %s%n",
                Base60.fromInt(1).add(Base60.fromFraction(1, 2)).add(Base60.fromFraction(1, 3)));
    }
}
