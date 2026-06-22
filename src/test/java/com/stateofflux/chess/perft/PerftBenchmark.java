package com.stateofflux.chess.perft;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Move-generation throughput benchmark (perft). Perft node counts are fixed reference values, so
 * this both checks correctness (asserts the exact counts) and measures speed (nodes/second).
 *
 * Throughput is a timing metric: machine-dependent and noisy, so it is NOT gated by a strict
 * baseline like the deterministic node-count metrics. Instead it is written to
 * perf-results/perft-bench.json (github-action-benchmark "customBiggerIsBetter" format) and tracked
 * over time by CI, which alerts on a large regression.
 *
 * Run with: mvn test -Dtest.groups=PerformanceTest -Dtest=PerftBenchmark -Djacoco.skip=true
 */
@Tag("PerformanceTest")
public class PerftBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final Path JSON = Path.of("perf-results", "perft-bench.json");

    private record Case(String name, String fen, int depth, long expectedNodes) {}

    private static final List<Case> CASES = List.of(
        new Case("startpos d5", FenString.INITIAL_BOARD, 5, 4_865_609L),
        new Case("kiwipete d4", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 4, 4_085_603L)
    );

    @BeforeAll
    public static void quietLogs() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
    }

    @Test public void measureThroughput() throws IOException {
        // Warm up the JIT before timing anything.
        new Game(FenString.INITIAL_BOARD).perft(4);

        logger.atWarn().log(String.format("%-14s %12s %8s %14s", "position", "nodes", "ms", "nodes/sec"));
        List<String> metrics = new ArrayList<>();

        for (Case c : CASES) {
            long start = System.nanoTime();
            long nodes = new Game(c.fen()).perft(c.depth());
            long millis = Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

            assertThat(nodes).as("perft %s", c.name()).isEqualTo(c.expectedNodes());

            long nps = nodes * 1000 / millis;
            logger.atWarn().log(String.format("%-14s %12d %8d %14d", c.name(), nodes, millis, nps));
            metrics.add(String.format(
                "  {\"name\": \"perft %s\", \"unit\": \"nodes/sec\", \"value\": %d}", c.name(), nps));
        }

        writeJson(metrics);
    }

    private void writeJson(List<String> metrics) throws IOException {
        StringJoiner json = new StringJoiner(",\n", "[\n", "\n]\n");
        metrics.forEach(json::add);
        Files.createDirectories(JSON.getParent());
        Files.writeString(JSON, json.toString());
        logger.atWarn().log("wrote {}", JSON);
    }
}
