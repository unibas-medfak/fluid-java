package ch.unibas.medizin.fluid;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generator for FlUIDs: compact, time-sortable unique identifiers.
 *
 * <p>A FlUID is a 16-character string encoding 80 bits in Crockford Base32:
 * a 40-bit millisecond timestamp followed by 40 random bits. Both halves are
 * encoded most-significant-first, so lexicographic order matches creation
 * order at millisecond granularity; IDs created within the same millisecond
 * have no defined relative order.
 *
 * <p>The alphabet ({@code 0123456789ABCDEFGHJKMNPQRSTVWXYZ}) is URL-safe,
 * case-insensitive, and excludes the easily confused letters I, L, O, and U.
 *
 * <p>This class is thread-safe: randomness comes from
 * {@link ThreadLocalRandom}, so {@link #generate()} involves no locking or
 * contention.
 *
 * <p><strong>Not a secret.</strong> With 40 random bits and a
 * non-cryptographic RNG, FlUID values are guessable in principle. Do not use
 * them as authentication tokens, session IDs, or unguessable URLs.
 */
public final class FlUID {

    /**
     * Custom epoch ({@code 2026-01-01T00:00:00Z}) as milliseconds since the
     * Unix epoch. The timestamp component counts milliseconds from here.
     */
    private static final long EPOCH_OFFSET_MS =
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli();

    /** Crockford Base32 alphabet (no I, L, O, U). */
    private static final char[] ALPHABET =
            "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    /** Bit mask selecting the low 40 bits of a {@code long}. */
    private static final long MASK_40 = (1L << 40) - 1;

    private FlUID() {
    }

    /**
     * Generates a new FlUID.
     *
     * @return a 16-character Crockford Base32 string whose first 8 characters
     *         encode the current time and whose last 8 characters are random
     * @throws IllegalStateException if the system clock is outside the
     *         representable 40-bit range (before 2026-01-01 or after roughly
     *         October 2060)
     */
    public static String generate() {
        long elapsed = System.currentTimeMillis() - EPOCH_OFFSET_MS;
        if (elapsed < 0 || elapsed > MASK_40) {
            throw new IllegalStateException("timestamp outside 40-bit range");
        }
        long random = ThreadLocalRandom.current().nextLong() & MASK_40;

        char[] out = new char[16];
        encode(elapsed, out, 0);
        encode(random, out, 8);
        return new String(out);
    }

    /**
     * Writes the low 40 bits of {@code value} as 8 Base32 characters into
     * {@code out}, most significant character first, starting at
     * {@code offset}.
     */
    private static void encode(long value, char[] out, int offset) {
        for (int i = 7; i >= 0; i--) {
            out[offset + i] = ALPHABET[(int) (value & 0x1F)];
            value >>>= 5;
        }
    }
}
