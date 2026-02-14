# Base60

Immutable Java implementation of a **sexagesimal (base-60) rational
number type** inspired by Babylonian mathematics.

Supports:

-   exact rational arithmetic (`BigInteger`-based)
-   base-60 formatting
-   periodic fraction detection
-   parsing from base-60 strings
-   comparison & sorting
-   conversion to `BigDecimal`

------------------------------------------------------------------------

## ✨ Features

-   Arbitrary precision rational numbers (`numerator / denominator`)
-   Exact arithmetic (no floating-point errors)
-   Automatic periodic fraction detection in base-60
-   Parsing format like:
```
    2:46:58
    2:46:58.30:15
    0.(8:34:17)
    -0:12:34
```
-   Implements `Comparable<Base60>`
-   Fully immutable
-   Fractions are normalized using GCD

------------------------------------------------------------------------

## 📦 Representation

Internally the number is stored as:

``` java
BigInteger numerator;
BigInteger denominator;
```

The fraction is always:

-   reduced via GCD
-   denominator \> 0
-   sign stored in numerator

Base-60 is only a **representation layer**, not the storage format.

------------------------------------------------------------------------

## 🚀 Usage

### Create from base-60 string

``` java
Base60 a = Base60.parse("2:46:58.30:15");
System.out.println(a.toDecimal());
```

------------------------------------------------------------------------

### Create from fraction

``` java
Base60 b = Base60.fromFraction(
    BigInteger.ONE,
    BigInteger.valueOf(7)
);

System.out.println(b.toBase60WithPeriod());
```

Output:
```
    0.(8:34:17)
```
------------------------------------------------------------------------

### Arithmetic

``` java
Base60 c = Base60.parse("1:30");
Base60 d = Base60.parse("2:15");

System.out.println(c.add(d));        // 3:45
System.out.println(c.compareTo(d));  // -1
```

Supported operations:

-   add
-   subtract
-   multiply
-   divide
-   compareTo
-   equals

All operations preserve exact rational precision.

------------------------------------------------------------------------

## 🔁 Periodic Fractions

Periodic detection works using exact remainder tracking:

Algorithm:

1.  Multiply remainder by 60
2.  Divide by denominator
3.  Track previously seen remainders
4.  When a remainder repeats → cycle detected

Example:
```
    1/7 → 0.(8:34:17)
```
------------------------------------------------------------------------

## 📐 Formatting

### Default (fixed precision)

``` java
System.out.println(b);
```

Uses default precision (e.g., 10 digits).

------------------------------------------------------------------------

### With detected period

``` java
System.out.println(b.toBase60WithPeriod());
```

Outputs canonical periodic representation.

------------------------------------------------------------------------

## 📊 Examples
```
  Rational   Base-60
  ---------- -------------
  1/2        0:30
  1/3        0:20
  1/6        0:10
  1/7        0.(8:34:17)
  10000      2:46:40
```
------------------------------------------------------------------------

## 🏛 Why Base-60?

60 has many divisors:
```
    1,2,3,4,5,6,10,12,15,20,30,60
```
That makes many fractions finite:

-   1/3 → finite
-   1/6 → finite
-   1/12 → finite

This is why Babylonian mathematics used base-60 and why we still use:

-   60 seconds
-   60 minutes
-   360 degrees

------------------------------------------------------------------------

## 🧠 Design Notes

-   No `double`
-   No precision loss
-   Period detection is mathematically exact
-   All arithmetic is rational

------------------------------------------------------------------------

## 📄 License

GNU General Public License v2.0

------------------------------------------------------------------------

## 🏗 Possible Extensions

-   Generic `BaseN` implementation
-   Parsing periodic input `0.(8:34:17)`
-   Implement `java.lang.Number`
-   Configurable output formatting
-   Performance optimizations for very large denominators

------------------------------------------------------------------------

If you like ancient mathematics or numeric systems --- this is a fun
playground.
