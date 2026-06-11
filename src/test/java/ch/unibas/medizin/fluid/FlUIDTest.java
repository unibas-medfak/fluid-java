package ch.unibas.medizin.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class FlUIDTest {

    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    @Test
    void generatesIdOfLength16() {
        assertEquals(16, FlUID.generate().length());
    }

    @Test
    void usesOnlyCrockfordBase32Alphabet() {
        String id = FlUID.generate();
        for (char c : id.toCharArray()) {
            assertTrue(ALPHABET.indexOf(c) >= 0,
                    () -> "unexpected character '" + c + "' in " + id);
        }
    }

    @Test
    void generatesUniqueIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            ids.add(FlUID.generate());
        }
        assertEquals(10_000, ids.size(), "expected all generated ids to be unique");
    }

    @Test
    void consecutiveIdsDiffer() {
        assertNotEquals(FlUID.generate(), FlUID.generate());
    }

    @Test
    void timestampPrefixDecodesToCurrentTime() {
        long before = System.currentTimeMillis();
        String id = FlUID.generate();
        long after = System.currentTimeMillis();

        long epochOffsetMs = Instant.parse("2026-01-01T00:00:00Z").toEpochMilli();
        long elapsed = 0;
        for (char c : id.substring(0, 8).toCharArray()) {
            elapsed = (elapsed << 5) | ALPHABET.indexOf(c);
        }
        long decoded = epochOffsetMs + elapsed;

        assertTrue(decoded >= before && decoded <= after,
                () -> "decoded timestamp " + decoded + " not in [" + before + ", " + after + "]");
    }

    @Test
    void generatesUniqueIdsAcrossThreads() throws Exception {
        int threads = 8;
        int idsPerThread = 5_000;
        Set<String> ids = ConcurrentHashMap.newKeySet();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < idsPerThread; i++) {
                        ids.add(FlUID.generate());
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }
        assertEquals(threads * idsPerThread, ids.size(),
                "expected all concurrently generated ids to be unique");
    }

    @Test
    void timestampPrefixIsMonotonicNonDecreasing() {
        String first = FlUID.generate();
        String second = FlUID.generate();
        // First 8 chars encode the elapsed-time component; lexical order matches
        // numeric order for Crockford base32, so it must never go backwards.
        assertTrue(first.substring(0, 8).compareTo(second.substring(0, 8)) <= 0,
                "timestamp prefix should be non-decreasing");
    }
}
