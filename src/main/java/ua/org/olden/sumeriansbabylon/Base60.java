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

    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);

    private final BigDecimal decimalValue;
    private static BigInteger numerator = null;
    private static BigInteger denominator = BigInteger.valueOf(1);

    // --- Конструктори ---
    private Base60(BigDecimal value) {
        this.decimalValue = value.stripTrailingZeros();
    }

    public static Base60 fromDecimal(BigDecimal value) {
        Base60.numerator = value.toBigInteger();
        return new Base60(value);
    }

    public static Base60 fromFraction(BigInteger numerator, BigInteger denominator) {
        Base60.numerator = numerator;
        Base60.denominator = denominator;
        return new Base60(new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), MC));
    }

    // --- Парсер типу 2:46:58.30:15 ---
    public static Base60 parse(String input) {
        Objects.requireNonNull(input);

        boolean negative = input.startsWith("-");
        if (negative) {
            input = input.substring(1);
        }

        String[] parts = input.split("\\.");

        BigDecimal integerPart = parseIntegerPart(parts[0]);

        BigDecimal fractionalPart = BigDecimal.ZERO;
        if (parts.length > 1) {
            fractionalPart = parseFractionalPart(parts[1]);
        }

        BigDecimal result = integerPart.add(fractionalPart);

        if (negative) {
            result = result.negate();
        }

        return new Base60(result);
    }

    private static BigDecimal parseIntegerPart(String part) {
        if (part.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String[] digits = part.split(":");

        BigDecimal result = BigDecimal.ZERO;

        for (String d : digits) {
            int digit = Integer.parseInt(d);
            if (digit < 0 || digit >= 60) {
                throw new IllegalArgumentException("Digit must be 0-59");
            }

            result = result.multiply(SIXTY).add(BigDecimal.valueOf(digit));
        }

        return result;
    }

    private static BigDecimal parseFractionalPart(String part) {
        String[] digits = part.split(":");

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal divisor = SIXTY;

        for (String d : digits) {
            int digit = Integer.parseInt(d);
            if (digit < 0 || digit >= 60) {
                throw new IllegalArgumentException("Digit must be 0-59");
            }

            result = result.add(
                    BigDecimal.valueOf(digit).divide(divisor, MC)
            );

            divisor = divisor.multiply(SIXTY);
        }

        return result;
    }

    // --- Конвертація в base-60 список розрядів ---
    public List<Integer> toBase60IntegerDigits() {
        BigDecimal abs = decimalValue.abs();
        BigInteger integerPart = abs.toBigInteger();

        if (integerPart.equals(BigInteger.ZERO)) {
            return List.of(0);
        }

        List<Integer> digits = new ArrayList<>();
        BigInteger sixty = BigInteger.valueOf(60);

        while (integerPart.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = integerPart.divideAndRemainder(sixty);
            digits.add(divRem[1].intValue());
            integerPart = divRem[0];
        }

        Collections.reverse(digits);
        return digits;
    }

    public List<Integer> toBase60FractionDigits(int precision) {
        BigDecimal abs = decimalValue.abs();
        BigDecimal fraction = abs.subtract(new BigDecimal(abs.toBigInteger()));

        List<Integer> digits = new ArrayList<>();

        for (int i = 0; i < precision; i++) {
            fraction = fraction.multiply(SIXTY, MC);
            int digit = fraction.intValue();
            digits.add(digit);
            fraction = fraction.subtract(BigDecimal.valueOf(digit));
            if (fraction.compareTo(BigDecimal.ZERO) == 0) {
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

        return decimalValue.signum() < 0 ? "-" + result : result;
    }

    public String toBase60WithPeriod() {

        BigInteger integerPart = numerator.divide(denominator);
        BigInteger remainder = numerator.remainder(denominator);

        StringBuilder result = new StringBuilder();

        result.append(Base60.fromDecimal(new BigDecimal(integerPart)).toString());

        if (remainder.equals(BigInteger.ZERO)) {
            return result.toString();
        }

        result.append(".");

        Map<BigInteger, Integer> seen = new HashMap<>();
        List<Integer> digits = new ArrayList<>();

        int index = 0;

        while (!remainder.equals(BigInteger.ZERO)) {

            if (seen.containsKey(remainder)) {
                int cycleStart = seen.get(remainder);

                for (int i = 0; i < digits.size(); i++) {
                    if (i == cycleStart) {
                        result.append("(");
                    }
                    result.append(digits.get(i));
                    if (i < digits.size() - 1) {
                        result.append(":");
                    }
                }
                result.append(")");
                return result.toString();
            }

            seen.put(remainder, index++);

            remainder = remainder.multiply(BigInteger.valueOf(60));
            BigInteger digit = remainder.divide(denominator);

            digits.add(digit.intValue());
            remainder = remainder.remainder(denominator);
        }

        for (int i = 0; i < digits.size(); i++) {
            result.append(digits.get(i));
            if (i < digits.size() - 1) {
                result.append(":");
            }
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return toString(10); // дефолтна точність
    }

    // --- Доступ до десяткового значення ---
    public BigDecimal toDecimal() {
        return decimalValue;
    }

    // --- Арифметика ---
    public Base60 add(Base60 other) {
        return new Base60(this.decimalValue.add(other.decimalValue, MC));
    }

    public Base60 subtract(Base60 other) {
        return new Base60(this.decimalValue.subtract(other.decimalValue, MC));
    }

    public Base60 multiply(Base60 other) {
        return new Base60(this.decimalValue.multiply(other.decimalValue, MC));
    }

    public Base60 divide(Base60 other) {
        return new Base60(this.decimalValue.divide(other.decimalValue, MC));
    }

    // --- Comparable ---
    @Override
    public int compareTo(Base60 other) {
        return this.decimalValue.compareTo(other.decimalValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Base60)) {
            return false;
        }
        Base60 base60 = (Base60) o;
        return decimalValue.compareTo(base60.decimalValue) == 0;
    }

    @Override
    public int hashCode() {
        return decimalValue.stripTrailingZeros().hashCode();
    }
}
