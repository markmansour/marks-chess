package com.stateofflux.chess.perft;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT;
import com.stateofflux.chess.model.player.PestoEvaluator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.stateofflux.chess.perft.PerfBaseline.Case;
import static com.stateofflux.chess.perft.PerfBaseline.CASES;
import static com.stateofflux.chess.perft.PerfBaseline.BASELINE;
import static com.stateofflux.chess.perft.PerfBaseline.HISTORY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Records the hash-move-ordering A/B baseline (issue #6). For each position it runs the same
 * fixed-depth search with ordering on and off in one process; the metric is nodes-to-depth, which is
 * deterministic and machine-independent, so re-running on unchanged code reproduces the file exactly.
 *
 *   - overwrites perf-results/baseline.csv  (committed contract: position,depth,nodes_on,nodes_off)
 *   - appends  perf-results/history.csv     (gitignored: timestamp,sha,jdk,...,ms_on,ms_off,nps)
 *
 * Run with: mvn test -Dtest.groups=PerformanceTest -Dtest=HashMoveOrderingBenchmark -Djacoco.skip=true
 */
@Tag("PerformanceTest")
public class HashMoveOrderingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeAll
    public static void quietLogs() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
    }

    @Test public void recordBaseline() throws IOException {
        String sha = PerfBaseline.gitSha();
        String jdk = System.getProperty("java.version");
        String timestamp = Instant.now().toString();

        logger.atWarn().log("Hash-move ordering A/B  (sha {}, jdk {})", sha, jdk);
        logger.atWarn().log(String.format("%-10s %5s %12s %12s %8s", "position", "depth", "nodes(on)", "nodes(off)", "saved"));

        StringBuilder baseline = new StringBuilder("position,depth,nodes_on,nodes_off\n");
        StringBuilder history = new StringBuilder();
        long totalOn = 0;
        long totalOff = 0;

        for (Case c : CASES) {
            Result off = search(c, false);   // warm up + measure the harder case first
            Result on = search(c, true);
            totalOn += on.nodes;
            totalOff += off.nodes;

            double saved = off.nodes == 0 ? 0 : (100.0 * (off.nodes - on.nodes) / off.nodes);
            long npsOn = on.millis == 0 ? 0 : on.nodes * 1000 / on.millis;

            logger.atWarn().log(String.format("%-10s %5d %12d %12d %7.1f%%", c.name(), c.depth(), on.nodes, off.nodes, saved));
            baseline.append(String.join(",", c.name(), String.valueOf(c.depth()),
                String.valueOf(on.nodes), String.valueOf(off.nodes))).append('\n');
            history.append(String.join(",", timestamp, sha, jdk, c.name(), String.valueOf(c.depth()),
                String.valueOf(on.nodes), String.valueOf(off.nodes),
                String.valueOf(on.millis), String.valueOf(off.millis), String.valueOf(npsOn))).append('\n');
        }

        double totalSaved = totalOff == 0 ? 0 : (100.0 * (totalOff - totalOn) / totalOff);
        logger.atWarn().log(String.format("%-10s %5s %12d %12d %7.1f%%", "TOTAL", "", totalOn, totalOff, totalSaved));

        writeBaseline(baseline.toString());
        appendHistory(history.toString());

        assertThat(totalOn).isLessThanOrEqualTo(totalOff);   // ordering must never cost nodes overall
    }

    private record Result(long nodes, long millis) {}

    private Result search(Case c, boolean hashMoveOrdering) {
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(c.depth());
        player.setIncrement(TimeUnit.MINUTES.toNanos(10));   // no timeout: fixed-depth, deterministic
        player.setHashMoveOrdering(hashMoveOrdering);

        long start = System.nanoTime();
        player.getNextMove(new Game(c.fen()));
        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new Result(player.getNodesVisited(), millis);
    }

    private void writeBaseline(String contents) throws IOException {
        Files.createDirectories(BASELINE.getParent());
        Files.writeString(BASELINE, contents);   // overwrite: deterministic, no churn on unchanged code
    }

    private void appendHistory(String rows) throws IOException {
        Files.createDirectories(HISTORY.getParent());
        boolean fresh = Files.notExists(HISTORY);
        String header = fresh ? "timestamp,git_sha,jdk,position,depth,nodes_on,nodes_off,ms_on,ms_off,nps_on\n" : "";
        Files.writeString(HISTORY, header + rows,
            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }
}
