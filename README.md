SumeriansBabylon
================

<code>        Base60 a = Base60.parse("2:46:58.30:15");
        System.out.println(a.toDecimal());

        Base60 b = Base60.fromFraction(BigInteger.ONE, BigInteger.valueOf(7));
        System.out.println(b);
        System.out.println(b.toBase60WithPeriod());

        Base60 c = Base60.parse("1:30");
        Base60 d = Base60.parse("2:15");

        System.out.println(c);
        System.out.println(d);

        System.out.println(c.add(d));
        System.out.println(c.compareTo(d));</code>

<pre>10018.5041666666666666666666666666666666666666666666666667
0.8:34:17:8:34:17:8:34:17:8
0.(8:34:17)
1:30
2:15
3:45
-1</pre>