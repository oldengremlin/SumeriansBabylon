package ua.org.olden.sumeriansbabylon;

import java.math.BigInteger;

/**
 *
 * @author olden
 */
public class SumeriansBabylon {

    public static void main(String[] args) {
        Base60 a = Base60.parse("2:46:58.30:15");
        System.out.print(a + " → ");
        System.out.println(a.toDecimal());

        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(7));
        Base60 e = Base60.parse("0.8:34:17");
        Base60 f = Base60.parse("0.(8:34:17)");

        System.out.print(b.toDecimal() + " → ");
        System.out.print(b + " → ");
        System.out.println(b.toBase60WithPeriod());

        System.out.print(e + " → ");
        System.out.println(e.toDecimal());

        System.out.print(f + " → ");
        System.out.println(f.toDecimal());

        System.out.println();

        Base60 c = Base60.parse("1:30");
        Base60 d = Base60.parse("2:15");

        System.out.print(c + " → ");
        System.out.println(c.toDecimal());

        System.out.print(d + " → ");
        System.out.println(d.toDecimal());

        System.out.print(c + " + " + d + " = ");
        System.out.println(c.add(d));

        System.out.print(c.toInteger() + " + " + d.toInteger() + " = ");
        System.out.print(c.toInteger().add(d.toInteger()) + " → ");
        System.out.println(Base60.fromInteger(c.toInteger().add(d.toInteger())));

        System.out.println();

        System.out.println(c.compareTo(d));
    }
}
