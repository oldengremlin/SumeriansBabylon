# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
mvn clean compile
mvn test
mvn test -Dtest=Base60Test
mvn test -Dtest=Base60Test#sqrtTwo
mvn exec:java                                                            # SumeriansBabylon (demo)
mvn exec:java -Dexec.mainClass=ua.org.olden.sumeriansbabylon.MulTable
```

## Architecture

### Core Design

`Base60` is an **immutable value object** (`extends Number implements Comparable<Base60>`) representing an exact rational number stored as a GCD-reduced `BigInteger` numerator/denominator pair (denominator always positive, sign always in numerator). Base-60 is purely a presentation layer — all arithmetic is exact rational arithmetic.

### Construction (factory methods only, no public constructor)

| Method | Notes |
|---|---|
| `fromInt(int)` / `fromLong(long)` / `fromInteger(BigInteger)` | Whole numbers |
| `fromFraction(num, den)` | Overloaded for `int`, `long`, `BigInteger` |
| `fromDecimal(BigDecimal)` | Converts via `unscaledValue / 10^scale` |
| `parse(String)` | Colon-separated base-60, e.g. `"2:46:58.30:15"` or `"-0:12"` |

`parse` format: digits before `.` are the integer part (each 0–59), digits after `.` are fractional sexagesimal places. Digits outside 0–59 throw `IllegalArgumentException`.

### Arithmetic

`add`, `subtract`, `multiply`, `divide`, `pow(int)`, `pow(Base60)`, `mod(Base60)` — all return new instances. `mod` uses **floor mod** (result has the sign of the divisor, like Python `%`). `pow(Base60)` falls back to `Math.pow` via `double` when the exponent is fractional.

`negate()`, `abs()`, `signum()` — sign utilities.

`sqrt()` — uses `BigDecimal.sqrt(MathContext(50))`.
`sqrtSumerians()` — Babylonian/Heron method: 10 iterations of `x = (x + S/x) / 2` seeded from `Math.sqrt`. Both methods produce the same result to 8+ base-60 places.

### Formatting

| Method | Output |
|---|---|
| `toString()` / `toString(int precision)` | `"2:46:58.30:15"`, default precision 10 |
| `toBase60WithPeriod()` | `"0.(8:34:17)"` — detects cycles via remainder tracking |
| `toSumerianString()` | Unicode cuneiform (U+12000–U+1247F block) |
| `toDecimal()` | `BigDecimal` at 50-digit precision (`MathContext.HALF_UP`) |
| `toInteger()` | `BigInteger`, truncates toward zero |

### Cuneiform Encoding

Characters are above U+FFFF — surrogate pairs required. Tests use `Character.toString(0x12079)` style (not source-level escapes). Digit mapping is built once in `buildCuneiformDigits()`:
- 0 → `𒑱` (U+12471)
- 1–9 → single composite glyphs (U+12079, U+12400–U+12407)
- 10/20/30/40/50 → repeated Winkelhaken `𒌋` (U+1230B)
- Fractional separator → `𒑲` (U+12472)

### Demo / Example Classes

`SumeriansBabylon` (default `exec.mainClass`) — exercises all major features.
`MulTable` — standalone multiplication table demo, cuneiform output.

### Testing

`Base60Test` uses JUnit Jupiter 6 with `@DisplayName` in Ukrainian and `@CsvSource` for parameterized cases. All 84 tests run in ~1 s.
