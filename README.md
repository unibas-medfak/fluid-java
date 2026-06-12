# FlUID

Compact, time-sortable unique identifiers for Java. A FlUID is a 16-character
string encoding 80 bits: a 40-bit millisecond timestamp followed by 40 random
bits, rendered in Crockford Base32.

```
0123456789ABCDEFGHJKMNPQRSTVWXYZ   (no I, L, O, U)

 0A3D7F2K 9XQ4MV8T
 └──────┘ └──────┘
 timestamp  random
  40 bits   40 bits
```

## Installation

FlUID is available from Maven Central.

Maven:

```xml
<dependency>
    <groupId>ch.unibas.medizin</groupId>
    <artifactId>fluid</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("ch.unibas.medizin:fluid:1.0.0")
```

## Usage

```java
import ch.unibas.medizin.fluid.FlUID;

String id = FlUID.generate();   // e.g. "0A3D7F2K9XQ4MV8T"
```

## Properties

- **Time-sortable.** The timestamp prefix is encoded most-significant-first,
  so lexicographic order matches creation order at millisecond granularity.
  IDs created within the same millisecond have no defined relative order.
- **Compact.** 16 characters versus 36 for a canonical UUID. The alphabet is
  URL-safe and case-insensitive, and excludes easily confused letters.
- **Collision-resistant.** 40 random bits per millisecond: generating even
  thousands of IDs in the same millisecond keeps the collision probability
  negligible (50% is only reached around one million IDs per millisecond).
- **Thread-safe and fast.** Randomness comes from `ThreadLocalRandom`, so
  `generate()` involves no locking or contention.
- **Not a secret.** With 40 random bits and a non-cryptographic RNG, FlUID
  values are guessable in principle. Do not use them as authentication
  tokens, session IDs, or unguessable URLs — use 128+ bits from
  `SecureRandom` for that.

## Timestamp range

The timestamp counts milliseconds since `2026-01-01T00:00:00Z`. A 40-bit
counter covers about 34.8 years, so IDs can be generated until roughly
October 2060. `generate()` throws `IllegalStateException` if the system
clock is outside this range (including clocks set before 2026).

## Requirements

- Java 21+
- Maven 4 (wrapper included)

## Build and test

```sh
./mvnw test
```
