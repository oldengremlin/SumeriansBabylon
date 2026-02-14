package ua.org.olden.sumeriansbabylon;

import java.math.BigInteger;

/**
 *
 * @author olden
 */
public class SumeriansBabylon {

    public static void main(String[] args) {
        Base60 a = Base60.parse("2:46:58.30:15");
        System.out.println(a.toDecimal());

        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(7));
        System.out.println(b);
        System.out.println(b.toBase60WithPeriod());

        Base60 c = Base60.parse("1:30");
        Base60 d = Base60.parse("2:15");

        System.out.println(c);
        System.out.println(d);

        System.out.println(c.add(d));     // 3:45
        System.out.println(c.compareTo(d)); // -1
    }
}
