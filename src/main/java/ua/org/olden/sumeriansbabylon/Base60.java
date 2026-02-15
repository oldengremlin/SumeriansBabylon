package ua.org.olden.sumeriansbabylon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Base60 implements Comparable<Base60> {

    private static final BigInteger SIXTY = BigInteger.valueOf(60);
    private static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);

    private final BigInteger numerator;
    private final BigInteger denominator;

    // --- Конструктори ---
    private Base60(BigInteger num, BigInteger den) {
        Objects.requireNonNull(num);
        Objects.requireNonNull(den);
        if (den.signum() == 0) {
            throw new ArithmeticException("Denominator cannot be zero");
        }
        // Нормалізація: GCD, знак в num, den > 0
        BigInteger gcd = num.gcd(den).abs();
        this.numerator = num.divide(gcd).multiply(BigInteger.valueOf(den.signum()));
        this.denominator = den.abs().divide(gcd);
    }

    public static Base60 fromInteger(BigInteger value) {
        return new Base60(value, BigInteger.ONE);
    }

    public static Base60 fromDecimal(BigDecimal value) {
        Objects.requireNonNull(value);
        // Конвертуємо BigDecimal в точний дріб: unscaled / 10^scale
        BigInteger num = value.unscaledValue();
        int scale = value.scale();
        BigInteger den = BigInteger.ONE;
        if (scale >= 0) {
            den = BigInteger.TEN.pow(scale);
        } else {
            num = num.multiply(BigInteger.TEN.pow(-scale));
        }
        return new Base60(num, den);
    }

    public static Base60 fromFraction(BigInteger num, BigInteger den) {
        return new Base60(num, den);
    }

    // --- Парсер типу 2:46:58.30:15 ---
    public static Base60 parse(String input) {
        Objects.requireNonNull(input);
        boolean negative = input.startsWith("-");
        if (negative) {
            input = input.substring(1);
        }
        String[] parts = input.split("\\.");
        BigInteger integerNum = parseIntegerPart(parts[0]);
        BigInteger fracNum = BigInteger.ZERO;
        BigInteger fracDen = BigInteger.ONE;
        if (parts.length > 1) {
            String[] fracDigits = parts[1].replaceAll("[()]", "").split(":");
            fracDen = SIXTY.pow(fracDigits.length);
            for (int i = 0; i < fracDigits.length; i++) {
                int digit = Integer.parseInt(fracDigits[i]);
                if (digit < 0 || digit >= 60) {
                    throw new IllegalArgumentException("Digit must be 0-59");
                }
                BigInteger power = SIXTY.pow(fracDigits.length - 1 - i);
                fracNum = fracNum.add(BigInteger.valueOf(digit).multiply(power));
            }
        }
        // Збираємо в один дріб: (integerNum * fracDen + fracNum) / fracDen
        BigInteger totalNum = integerNum.multiply(fracDen).add(fracNum);
        if (negative) {
            totalNum = totalNum.negate();
        }
        return new Base60(totalNum, fracDen);
    }

    private static BigInteger parseIntegerPart(String part) {
        if (part.isEmpty()) {
            return BigInteger.ZERO;
        }
        String[] digits = part.split(":");
        BigInteger result = BigInteger.ZERO;
        for (String d : digits) {
            int digit = Integer.parseInt(d);
            if (digit < 0 || digit >= 60) {
                throw new IllegalArgumentException("Digit must be 0-59");
            }
            result = result.multiply(SIXTY).add(BigInteger.valueOf(digit));
        }
        return result;
    }

    // --- Конвертація в base-60 список розрядів ---
    public List<Integer> toBase60IntegerDigits() {
        BigInteger absIntPart = numerator.abs().divide(denominator);  // Ціла частина
        if (absIntPart.equals(BigInteger.ZERO)) {
            return List.of(0);
        }
        List<Integer> digits = new ArrayList<>();
        while (absIntPart.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = absIntPart.divideAndRemainder(SIXTY);
            digits.add(divRem[1].intValue());
            absIntPart = divRem[0];
        }
        Collections.reverse(digits);
        return digits;
    }

    public List<Integer> toBase60FractionDigits(int precision) {
        BigInteger absRemainder = numerator.abs().remainder(denominator);
        if (absRemainder.equals(BigInteger.ZERO)) {
            return Collections.emptyList();
        }
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i < precision; i++) {
            absRemainder = absRemainder.multiply(SIXTY);
            BigInteger digit = absRemainder.divide(denominator);
            digits.add(digit.intValue());
            absRemainder = absRemainder.remainder(denominator);
            if (absRemainder.equals(BigInteger.ZERO)) {
                break;
            }
        }
        return digits;
    }

    // --- Форматування ---
    public String toString(int precision) {
        List<Integer> intDigits = toBase60IntegerDigits();
        List<Integer> fracDigits = toBase60FractionDigits(precision);

        // --- обрізання нулів у кінці ---
        int lastNonZero = fracDigits.size() - 1;
        while (lastNonZero >= 0 && fracDigits.get(lastNonZero) == 0) {
            lastNonZero--;
        }

        if (lastNonZero >= 0) {
            fracDigits = fracDigits.subList(0, lastNonZero + 1);
        } else {
            fracDigits = Collections.emptyList();
        }

        String intPart = intDigits.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(":"));

        String result = intPart;

        if (!fracDigits.isEmpty()) {
            String fracPart = fracDigits.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(":"));
            result += "." + fracPart;
        }

        return numerator.signum() < 0 ? "-" + result : result;
    }

    public String toBase60WithPeriod() {
        boolean negative = numerator.signum() < 0;
        BigInteger absNum = numerator.abs();
        BigInteger intPart = absNum.divide(denominator);
        BigInteger remainder = absNum.remainder(denominator);

        StringBuilder sb = new StringBuilder();

        // Ціла частина
        List<Integer> intDigits = toBase60IntegerDigitsFor(intPart);  // див. нижче
        String intStr = intDigits.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
        sb.append(intStr);

        if (remainder.equals(BigInteger.ZERO)) {
            return negative ? "-" + sb : sb.toString();
        }

        sb.append(".");

        Map<BigInteger, Integer> seen = new HashMap<>();
        List<Integer> digits = new ArrayList<>();
        Integer cycleStart = null;

        BigInteger current = remainder;
        while (!current.equals(BigInteger.ZERO)) {
            if (seen.containsKey(current)) {
                cycleStart = seen.get(current);
                break;
            }
            seen.put(current, digits.size());

            current = current.multiply(SIXTY);
            BigInteger digit = current.divide(denominator);
            digits.add(digit.intValue());
            current = current.remainder(denominator);
        }

        for (int i = 0; i < digits.size(); i++) {
            if (cycleStart != null && i == cycleStart) {
                sb.append("(");
            }
            sb.append(digits.get(i));
            if (i < digits.size() - 1) {
                sb.append(":");
            }
        }

        if (cycleStart != null) {
            sb.append(")");
        }

        return negative ? "-" + sb : sb.toString();
    }

    private List<Integer> toBase60IntegerDigitsFor(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return List.of(0);
        }
        List<Integer> digits = new ArrayList<>();
        while (n.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = n.divideAndRemainder(SIXTY);
            digits.add(divRem[1].intValue());
            n = divRem[0];
        }
        Collections.reverse(digits);
        return digits;
    }

    @Override
    public String toString() {
        return toString(10); // дефолтна точність
    }

    // --- Доступ до десяткового значення ---
    public BigDecimal toDecimal() {
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), MC);
    }

    public BigInteger toInteger() {
        return numerator.divide(denominator);
    }

    // --- Арифметика ---
    public Base60 add(Base60 other) {
        BigInteger newNum = this.numerator.multiply(other.denominator)
                .add(other.numerator.multiply(this.denominator));
        BigInteger newDen = this.denominator.multiply(other.denominator);
        return new Base60(newNum, newDen);
    }

    public Base60 subtract(Base60 other) {
        BigInteger newNum = this.numerator.multiply(other.denominator)
                .subtract(other.numerator.multiply(this.denominator));
        BigInteger newDen = this.denominator.multiply(other.denominator);
        return new Base60(newNum, newDen);
    }

    public Base60 multiply(Base60 other) {
        BigInteger newNum = this.numerator.multiply(other.numerator);
        BigInteger newDen = this.denominator.multiply(other.denominator);
        return new Base60(newNum, newDen);
    }

    public Base60 divide(Base60 other) {
        if (other.numerator.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        BigInteger newNum = this.numerator.multiply(other.denominator);
        BigInteger newDen = this.denominator.multiply(other.numerator);
        return new Base60(newNum, newDen);
    }

    // --- Comparable ---
    @Override
    public int compareTo(Base60 other) {
        BigInteger left = this.numerator.multiply(other.denominator);
        BigInteger right = other.numerator.multiply(this.denominator);
        return left.compareTo(right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Base60)) {
            return false;
        }
        Base60 other = (Base60) o;
        return this.compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }
}
