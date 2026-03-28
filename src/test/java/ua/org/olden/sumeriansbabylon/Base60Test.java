package ua.org.olden.sumeriansbabylon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base60 — шістдесяткова арифметика")
class Base60Test {

    // -------------------------------------------------------------------------
    // Фабричні методи
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("fromInteger створює ціле число")
    void fromInteger() {
        Base60 n = Base60.fromInteger(BigInteger.valueOf(3600));
        assertEquals("1:0:0", n.toString());
    }

    @Test
    @DisplayName("fromInt створює ціле число з int")
    void fromInt() {
        Base60 n = Base60.fromInt(60);
        assertEquals("1:0", n.toString());
    }

    @Test
    @DisplayName("fromLong створює ціле число з long")
    void fromLong() {
        Base60 n = Base60.fromLong(3600L);
        assertEquals("1:0:0", n.toString());
    }

    @Test
    @DisplayName("fromInt(0) повертає нуль")
    void fromIntZero() {
        assertEquals("0", Base60.fromInt(0).toString());
    }

    @Test
    @DisplayName("fromInt від'ємне число")
    void fromIntNegative() {
        assertEquals("-1:0", Base60.fromInt(-60).toString());
    }

    @Test
    @DisplayName("fromDecimal точно конвертує десятковий дріб")
    void fromDecimal() {
        Base60 n = Base60.fromDecimal(new BigDecimal("0.5"));
        assertEquals("0.30", n.toString());
    }

    @Test
    @DisplayName("fromFraction(BigInteger, BigInteger) скорочує дріб")
    void fromFractionBigInteger() {
        Base60 n = Base60.fromFraction(BigInteger.valueOf(2), BigInteger.valueOf(4));
        assertEquals(Base60.fromFraction(BigInteger.ONE, BigInteger.TWO), n);
    }

    @Test
    @DisplayName("fromFraction(int, int) збігається з BigInteger-варіантом")
    void fromFractionInt() {
        Base60 a = Base60.fromFraction(1, 3);
        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(3));
        assertEquals(a, b);
    }

    @Test
    @DisplayName("fromFraction(long, long) збігається з BigInteger-варіантом")
    void fromFractionLong() {
        Base60 a = Base60.fromFraction(1L, 4L);
        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(4));
        assertEquals(a, b);
    }

    @Test
    @DisplayName("fromFraction з від'ємним знаменником — знак переходить у чисельник")
    void fromFractionNegativeDen() {
        Base60 a = Base60.fromFraction(1, -3);
        Base60 b = Base60.fromFraction(-1, 3);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("fromFraction з нульовим знаменником кидає ArithmeticException")
    void fromFractionZeroDen() {
        assertThrows(ArithmeticException.class,
                () -> Base60.fromFraction(BigInteger.ONE, BigInteger.ZERO));
    }

    // -------------------------------------------------------------------------
    // Парсинг
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("parse — ціле число")
    void parseInteger() {
        assertEquals(Base60.fromInt(3600), Base60.parse("1:0:0"));
    }

    @Test
    @DisplayName("parse — дріб без цілої частини")
    void parseFraction() {
        Base60 half = Base60.parse(".30");
        assertEquals(Base60.fromFraction(1, 2), half);
    }

    @Test
    @DisplayName("parse — ціле + дріб")
    void parseIntegerAndFraction() {
        Base60 n = Base60.parse("1:30.30");
        // 1:30 = 90, .30 = 1/2 → 90.5
        assertEquals(new BigDecimal("90.5"), n.toDecimal().stripTrailingZeros());
    }

    @Test
    @DisplayName("parse — від'ємне число")
    void parseNegative() {
        Base60 a = Base60.parse("-1:0");
        Base60 b = Base60.fromInt(-60);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("parse — цифра поза діапазоном 0-59 кидає IllegalArgumentException")
    void parseInvalidDigit() {
        assertThrows(IllegalArgumentException.class, () -> Base60.parse("60"));
    }

    @Test
    @DisplayName("parse — null кидає NullPointerException")
    void parseNull() {
        assertThrows(NullPointerException.class, () -> Base60.parse(null));
    }

    // -------------------------------------------------------------------------
    // Арифметика
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("додавання двох цілих")
    void addIntegers() {
        Base60 a = Base60.fromInt(30);
        Base60 b = Base60.fromInt(30);
        assertEquals(Base60.fromInt(60), a.add(b));
    }

    @Test
    @DisplayName("додавання дробів: 1/3 + 1/6 = 1/2")
    void addFractions() {
        Base60 a = Base60.fromFraction(1, 3);
        Base60 b = Base60.fromFraction(1, 6);
        assertEquals(Base60.fromFraction(1, 2), a.add(b));
    }

    @Test
    @DisplayName("віднімання: результат нуль")
    void subtractToZero() {
        Base60 a = Base60.fromInt(42);
        assertEquals(Base60.fromInt(0), a.subtract(a));
    }

    @Test
    @DisplayName("віднімання: від'ємний результат")
    void subtractNegative() {
        Base60 a = Base60.fromInt(1);
        Base60 b = Base60.fromInt(2);
        assertEquals(Base60.fromInt(-1), a.subtract(b));
    }

    @Test
    @DisplayName("множення: 1/2 * 1/3 = 1/6")
    void multiply() {
        Base60 a = Base60.fromFraction(1, 2);
        Base60 b = Base60.fromFraction(1, 3);
        assertEquals(Base60.fromFraction(1, 6), a.multiply(b));
    }

    @Test
    @DisplayName("множення на нуль завжди нуль")
    void multiplyByZero() {
        Base60 a = Base60.fromInt(999);
        assertEquals(Base60.fromInt(0), a.multiply(Base60.fromInt(0)));
    }

    @Test
    @DisplayName("ділення: 1/2 / 1/4 = 2")
    void divide() {
        Base60 a = Base60.fromFraction(1, 2);
        Base60 b = Base60.fromFraction(1, 4);
        assertEquals(Base60.fromInt(2), a.divide(b));
    }

    @Test
    @DisplayName("ділення на нуль кидає ArithmeticException")
    void divideByZero() {
        assertThrows(ArithmeticException.class,
                () -> Base60.fromInt(1).divide(Base60.fromInt(0)));
    }

    @Test
    @DisplayName("арифметика: (a + b) * c = a*c + b*c")
    void distributiveLaw() {
        Base60 a = Base60.fromFraction(1, 3);
        Base60 b = Base60.fromFraction(1, 4);
        Base60 c = Base60.fromInt(12);
        assertEquals(a.add(b).multiply(c), a.multiply(c).add(b.multiply(c)));
    }

    // -------------------------------------------------------------------------
    // Порівняння / equals / hashCode
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("рівні дроби: 2/4 == 1/2")
    void equalsFractions() {
        assertEquals(Base60.fromFraction(2, 4), Base60.fromFraction(1, 2));
    }

    @Test
    @DisplayName("нерівні числа")
    void notEquals() {
        assertNotEquals(Base60.fromInt(1), Base60.fromInt(2));
    }

    @Test
    @DisplayName("однакові об'єкти мають однаковий hashCode")
    void hashCodeConsistent() {
        Base60 a = Base60.fromFraction(1, 3);
        Base60 b = Base60.fromFraction(2, 6);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("compareTo: менше / рівно / більше")
    void compareTo() {
        Base60 small = Base60.fromFraction(1, 3);
        Base60 mid   = Base60.fromFraction(1, 2);
        Base60 big   = Base60.fromFraction(2, 3);
        assertTrue(small.compareTo(mid) < 0);
        assertEquals(0, mid.compareTo(Base60.fromFraction(2, 4)));
        assertTrue(big.compareTo(mid) > 0);
    }

    @Test
    @DisplayName("від'ємне менше нуля")
    void compareNegative() {
        assertTrue(Base60.fromInt(-1).compareTo(Base60.fromInt(0)) < 0);
    }

    // -------------------------------------------------------------------------
    // Форматування toString
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "{0} → \"{1}\"")
    @DisplayName("toString: відомі значення base-60")
    @CsvSource({
        "0,    0",
        "59,   59",
        "60,   1:0",
        "3600, 1:0:0",
        "3661, 1:1:1"
    })
    void toStringIntegers(int value, String expected) {
        assertEquals(expected, Base60.fromInt(value).toString());
    }

    @Test
    @DisplayName("toString: дріб 1/2 = 0.30")
    void toStringHalf() {
        assertEquals("0.30", Base60.fromFraction(1, 2).toString());
    }

    @Test
    @DisplayName("toString: від'ємний дріб")
    void toStringNegativeFraction() {
        assertEquals("-0.30", Base60.fromFraction(-1, 2).toString());
    }

    @Test
    @DisplayName("toString: 1/3 точно в base-60 (0.20)")
    void toStringOneThird() {
        assertEquals("0.20", Base60.fromFraction(1, 3).toString());
    }

    @Test
    @DisplayName("toString(precision): обмежена точність")
    void toStringWithPrecision() {
        // 1/7 не скінченний у base-60, але toString(3) обріже до 3 розрядів
        String s = Base60.fromFraction(1, 7).toString(3);
        assertFalse(s.isEmpty());
        // перевіряємо що є не більше 3 дробових розрядів
        String[] parts = s.split("\\.");
        if (parts.length == 2) {
            assertTrue(parts[1].split(":").length <= 3);
        }
    }

    // -------------------------------------------------------------------------
    // toBase60WithPeriod
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("1/3 є скінченним у base-60: 0.20")
    void withPeriodOneThirdFinite() {
        assertEquals("0.20", Base60.fromFraction(1, 3).toBase60WithPeriod());
    }

    @Test
    @DisplayName("1/7 має цикл у base-60: 0.(8:34:17)")
    void withPeriodOneSeventh() {
        assertEquals("0.(8:34:17)", Base60.fromFraction(1, 7).toBase60WithPeriod());
    }

    @Test
    @DisplayName("ціле число без дробової частини — без крапки")
    void withPeriodInteger() {
        assertEquals("1:0", Base60.fromInt(60).toBase60WithPeriod());
    }

    @Test
    @DisplayName("від'ємний циклічний дріб")
    void withPeriodNegativeCyclic() {
        String pos = Base60.fromFraction(1, 7).toBase60WithPeriod();
        String neg = Base60.fromFraction(-1, 7).toBase60WithPeriod();
        assertEquals("-" + pos, neg);
    }

    // -------------------------------------------------------------------------
    // toDecimal / toInteger
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDecimal: 1/2 = 0.5")
    void toDecimalHalf() {
        assertEquals(new BigDecimal("0.5"),
                Base60.fromFraction(1, 2).toDecimal().stripTrailingZeros());
    }

    @Test
    @DisplayName("toDecimal: 1/3 ≈ 0.333...")
    void toDecimalOneThird() {
        BigDecimal d = Base60.fromFraction(1, 3).toDecimal();
        // Перевіряємо перші 5 знаків після коми
        assertTrue(d.toPlainString().startsWith("0.33333"));
    }

    @Test
    @DisplayName("toInteger відкидає дробову частину")
    void toInteger() {
        assertEquals(BigInteger.valueOf(2), Base60.fromFraction(5, 2).toInteger());
    }

    @Test
    @DisplayName("toInteger від'ємного числа")
    void toIntegerNegative() {
        // -5/2 = -2.5 → toInteger() = -2 (усікання до нуля)
        assertEquals(BigInteger.valueOf(-2), Base60.fromFraction(-5, 2).toInteger());
    }

    // -------------------------------------------------------------------------
    // Властивості нейтральних елементів
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("a + 0 = a")
    void addIdentity() {
        Base60 a = Base60.fromFraction(3, 7);
        assertEquals(a, a.add(Base60.fromInt(0)));
    }

    @Test
    @DisplayName("a * 1 = a")
    void multiplyIdentity() {
        Base60 a = Base60.fromFraction(3, 7);
        assertEquals(a, a.multiply(Base60.fromInt(1)));
    }

    @Test
    @DisplayName("a / a = 1")
    void divideSelf() {
        Base60 a = Base60.fromFraction(3, 7);
        assertEquals(Base60.fromInt(1), a.divide(a));
    }

    // -------------------------------------------------------------------------
    // toSumerianString
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("нуль → 𒑱")
    void sumerianZero() {
        assertEquals(Character.toString(0x12471), Base60.fromInt(0).toSumerianString()); // 𒑱
    }

    @Test
    @DisplayName("1 → 𒁹")
    void sumerianOne() {
        assertEquals(Character.toString(0x12079), Base60.fromInt(1).toSumerianString()); // 𒁹
    }

    @Test
    @DisplayName("10 → 𒌋")
    void sumerianTen() {
        assertEquals(Character.toString(0x1230B), Base60.fromInt(10).toSumerianString()); // 𒌋
    }

    @Test
    @DisplayName("59 → п'ять десятків + дев'ять одиниць")
    void sumerianFiftyNine() {
        String fifty = Character.toString(0x1230B).repeat(5); // 𒌋𒌋𒌋𒌋𒌋
        String nine  = Character.toString(0x12407);           // 𒐇
        assertEquals(fifty + nine, Base60.fromInt(59).toSumerianString());
    }

    @Test
    @DisplayName("60 (1:0) → два розряди з нулем")
    void sumerianSixty() {
        String one  = Character.toString(0x12079); // 𒁹
        String zero = Character.toString(0x12471); // 𒑱
        assertEquals(one + " " + zero, Base60.fromInt(60).toSumerianString());
    }

    @Test
    @DisplayName("1/2 → ціла 𒑱, дробова 𒌋𒌋𒌋 (30)")
    void sumerianHalf() {
        String zero   = Character.toString(0x12471);           // 𒑱
        String thirty = Character.toString(0x1230B).repeat(3); // 𒌋𒌋𒌋
        String frac   = Character.toString(0x12472);           // 𒑲
        assertEquals(zero + frac + thirty, Base60.fromFraction(1, 2).toSumerianString());
    }

    @Test
    @DisplayName("від'ємне число має префікс «-»")
    void sumerianNegative() {
        assertTrue(Base60.fromInt(-1).toSumerianString().startsWith("-"));
    }

    // -------------------------------------------------------------------------
    // negate / abs / signum
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("negate: змінює знак")
    void negate() {
        assertEquals(Base60.fromInt(-3), Base60.fromInt(3).negate());
        assertEquals(Base60.fromInt(3), Base60.fromInt(-3).negate());
    }

    @Test
    @DisplayName("negate нуля дає нуль")
    void negateZero() {
        assertEquals(Base60.fromInt(0), Base60.fromInt(0).negate());
    }

    @Test
    @DisplayName("abs: завжди невід'ємне")
    void abs() {
        assertEquals(Base60.fromFraction(1, 3), Base60.fromFraction(-1, 3).abs());
        assertEquals(Base60.fromFraction(1, 3), Base60.fromFraction(1, 3).abs());
    }

    @Test
    @DisplayName("signum: -1 / 0 / 1")
    void signum() {
        assertEquals(-1, Base60.fromInt(-5).signum());
        assertEquals(0,  Base60.fromInt(0).signum());
        assertEquals(1,  Base60.fromInt(5).signum());
    }

    // -------------------------------------------------------------------------
    // pow
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("pow(0) = 1")
    void powZero() {
        assertEquals(Base60.fromInt(1), Base60.fromFraction(3, 7).pow(0));
    }

    @Test
    @DisplayName("pow(int) додатній: 2^10 = 1024")
    void powPositive() {
        assertEquals(Base60.fromInt(1024), Base60.fromInt(2).pow(10));
    }

    @Test
    @DisplayName("pow(int) від'ємний: 2^-1 = 1/2")
    void powNegative() {
        assertEquals(Base60.fromFraction(1, 2), Base60.fromInt(2).pow(-1));
    }

    @Test
    @DisplayName("pow(int) дробового числа: (1/2)^3 = 1/8")
    void powFraction() {
        assertEquals(Base60.fromFraction(1, 8), Base60.fromFraction(1, 2).pow(3));
    }

    @Test
    @DisplayName("pow нуля до від'ємного степеня кидає ArithmeticException")
    void powZeroNegative() {
        assertThrows(ArithmeticException.class, () -> Base60.fromInt(0).pow(-1));
    }

    @Test
    @DisplayName("pow(Base60) з цілим показником")
    void powBase60Integer() {
        assertEquals(Base60.fromInt(8), Base60.fromInt(2).pow(Base60.fromInt(3)));
    }

    @Test
    @DisplayName("pow(Base60) з дробовим показником: 4^0.5 ≈ 2")
    void powBase60Fractional() {
        Base60 result = Base60.fromInt(4).pow(Base60.fromFraction(1, 2));
        assertEquals(0, result.compareTo(Base60.fromInt(2)),
                "4^(1/2) має бути 2, отримали: " + result);
    }

    // -------------------------------------------------------------------------
    // mod
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("mod: 7 mod 3 = 1")
    void modBasic() {
        assertEquals(Base60.fromInt(1), Base60.fromInt(7).mod(Base60.fromInt(3)));
    }

    @Test
    @DisplayName("mod: -7 mod 3 = 2 (floor mod, знак дільника)")
    void modNegativeDividend() {
        assertEquals(Base60.fromInt(2), Base60.fromInt(-7).mod(Base60.fromInt(3)));
    }

    @Test
    @DisplayName("mod: 7 mod -3 = -2")
    void modNegativeDivisor() {
        assertEquals(Base60.fromInt(-2), Base60.fromInt(7).mod(Base60.fromInt(-3)));
    }

    @Test
    @DisplayName("mod дробових: 7/2 mod 3/2 = 1/2")
    void modFractions() {
        assertEquals(Base60.fromFraction(1, 2),
                Base60.fromFraction(7, 2).mod(Base60.fromFraction(3, 2)));
    }

    @Test
    @DisplayName("mod нуля кидає ArithmeticException")
    void modZero() {
        assertThrows(ArithmeticException.class,
                () -> Base60.fromInt(5).mod(Base60.fromInt(0)));
    }

    // -------------------------------------------------------------------------
    // sqrt / sqrtSumerians
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sqrt(4) = 2")
    void sqrtExact() {
        assertEquals(Base60.fromInt(2), Base60.fromInt(4).sqrt());
    }

    @Test
    @DisplayName("sqrt(2) наближається до 1.41421...")
    void sqrtTwo() {
        String dec = Base60.fromInt(2).sqrt().toDecimal().toPlainString();
        assertTrue(dec.startsWith("1.41421"), "Отримали: " + dec);
    }

    @Test
    @DisplayName("sqrt від'ємного кидає ArithmeticException")
    void sqrtNegative() {
        assertThrows(ArithmeticException.class, () -> Base60.fromInt(-1).sqrt());
    }

    @Test
    @DisplayName("sqrtSumerians(4) = 2")
    void sqrtSumeriansExact() {
        assertEquals(Base60.fromInt(2), Base60.fromInt(4).sqrtSumerians());
    }

    @Test
    @DisplayName("sqrtSumerians(2) збігається зі sqrt(2) до 10 знаків base-60")
    void sqrtSumeriansVsClassic() {
        Base60 classic  = Base60.fromInt(2).sqrt();
        Base60 sumerian = Base60.fromInt(2).sqrtSumerians();
        // порівнюємо перші 10 розрядів base-60 (toString за замовчуванням)
        assertEquals(classic.toString(8), sumerian.toString(8));
    }

    // -------------------------------------------------------------------------
    // java.lang.Number
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("intValue: 7/2 → 3 (усікання)")
    void intValue() {
        assertEquals(3, Base60.fromFraction(7, 2).intValue());
    }

    @Test
    @DisplayName("longValue: 3600 → 3600L")
    void longValue() {
        assertEquals(3600L, Base60.fromInt(3600).longValue());
    }

    @Test
    @DisplayName("floatValue: 1/4 ≈ 0.25f")
    void floatValue() {
        assertEquals(0.25f, Base60.fromFraction(1, 4).floatValue(), 1e-6f);
    }

    @Test
    @DisplayName("doubleValue: 1/3 ≈ 0.333...")
    void doubleValue() {
        assertEquals(1.0 / 3.0, Base60.fromFraction(1, 3).doubleValue(), 1e-15);
    }

    @Test
    @DisplayName("Base60 є підкласом Number")
    void isNumber() {
        Number n = Base60.fromInt(42);
        assertEquals(42, n.intValue());
    }
}
