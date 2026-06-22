package com.stateofflux.chess.perft;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.FenString;
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
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * In-process A/B measurement of hash-move ordering (issue #6). For each position the same fixed-depth
 * search runs with ordering on and off; the metric is nodes-to-depth (ordering changes how many nodes
 * are visited, not how fast each is visited). Results are appended to perf-results/hash-move-ab.csv,
 * tagged with the git SHA and JDK, so the gain can be tracked over time.
 *
 * Run with: mvn test -Dgroups=PerformanceTest -Dtest=HashMoveOrderingBenchmark -Djacoco.skip=true
 */
@Tag("PerformanceTest")
public class HashMoveOrderingBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int DEPTH = 5;
    private static final Path CSV = Path.of("perf-results", "hash-move-ab.csv");

    private record Position(String name, String fen) {}

    private static final List<Position> POSITIONS = List.of(
        new Position("startpos", FenString.INITIAL_BOARD),
        new Position("kiwipete", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"),
        new Position("midgame",  "r1bq1rk1/pp2bppp/2n2n2/2pp4/3P4/2N1PN2/PP2BPPP/R1BQ1RK1 w - - 0 9"),
        new Position("endgame",  "8/2k5/3p4/p2P1p2/P2P1P2/8/8/3K4 w - - 0 1")
    );

    @BeforeAll
    public static void quietLogs() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
    }

    @Test public void measureHashMoveOrdering() throws IOException {
        String sha = gitSha();
        String jdk = System.getProperty("java.version");
        String timestamp = Instant.now().toString();

        logger.atWarn().log("Hash-move ordering A/B (depth {}), sha {}, jdk {}", DEPTH, sha, jdk);
        logger.atWarn().log(String.format("%-10s %12s %12s %8s", "position", "nodes(on)", "nodes(off)", "saved"));

        long totalOn = 0;
        long totalOff = 0;
        StringBuilder csv = new StringBuilder();

        for (Position p : POSITIONS) {
            long nodesOff = search(p.fen(), false);   // warm up + measure the harder case first
            long nodesOn = search(p.fen(), true);
            totalOn += nodesOn;
            totalOff += nodesOff;

            double saved = nodesOff == 0 ? 0 : (100.0 * (nodesOff - nodesOn) / nodesOff);
            logger.atWarn().log(String.format("%-10s %12d %12d %7.1f%%", p.name(), nodesOn, nodesOff, saved));
            csv.append(String.join(",",
                timestamp, sha, jdk, p.name(), String.valueOf(DEPTH),
                String.valueOf(nodesOn), String.valueOf(nodesOff), String.format("%.1f", saved))).append('\n');
        }

        double totalSaved = totalOff == 0 ? 0 : (100.0 * (totalOff - totalOn) / totalOff);
        logger.atWarn().log(String.format("%-10s %12d %12d %7.1f%%", "TOTAL", totalOn, totalOff, totalSaved));

        appendCsv(csv.toString());

        // Hash-move ordering should never visit more nodes overall than searching without it.
        assertThat(totalOn).isLessThanOrEqualTo(totalOff);
    }

    private long search(String fen, boolean hashMoveOrdering) {
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(DEPTH);
        player.setIncrement(TimeUnit.MINUTES.toNanos(10));   // no timeout: fixed-depth search
        player.setHashMoveOrdering(hashMoveOrdering);
        player.getNextMove(new Game(fen));
        return player.getNodesVisited();
    }

    private void appendCsv(String rows) throws IOException {
        Files.createDirectories(CSV.getParent());
        boolean fresh = Files.notExists(CSV);
        StringBuilder out = new StringBuilder();
        if (fresh)
            out.append("timestamp,git_sha,jdk,position,depth,nodes_on,nodes_off,saved_pct\n");
        out.append(rows);
        Files.writeString(CSV, out.toString(),
            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }

    private String gitSha() {
        try {
            Process p = new ProcessBuilder("git", "rev-parse", "--short", "HEAD").start();
            String sha = new String(p.getInputStream().readAllBytes()).strip();
            p.waitFor(2, TimeUnit.SECONDS);
            return sha.isBlank() ? "unknown" : sha;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
