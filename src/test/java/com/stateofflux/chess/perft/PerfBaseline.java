package com.stateofflux.chess.perft;

import com.stateofflux.chess.model.FenString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Shared definitions for the performance baseline: the position suite, the file locations, and a
 * reader for the committed baseline. Keeping these in one place means the recorder
 * ({@link HashMoveOrderingBenchmark}) and the regression test
 * ({@link HashMoveOrderingRegressionTest}) cannot drift apart.
 */
public final class PerfBaseline {
    private PerfBaseline() {}

    /** A position and the fixed depth it is searched to. */
    public record Case(String name, String fen, int depth) {}

    /** A row of the committed baseline. */
    public record Baseline(int depth, long nodesOn, long nodesOff) {}

    public static final Path BASELINE = Path.of("perf-results", "baseline.csv");
    public static final Path HISTORY = Path.of("perf-results", "history.csv");

    public static final List<Case> CASES = List.of(
        new Case("startpos", FenString.INITIAL_BOARD, 5),
        new Case("kiwipete", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 5),
        new Case("midgame",  "r1bq1rk1/pp2bppp/2n2n2/2pp4/3P4/2N1PN2/PP2BPPP/R1BQ1RK1 w - - 0 9", 5),
        new Case("endgame",  "8/2k5/3p4/p2P1p2/P2P1P2/8/8/3K4 w - - 0 1", 5)
    );

    /** Reads the committed baseline keyed by position name. Empty map if the file is absent. */
    public static Map<String, Baseline> read() throws IOException {
        Map<String, Baseline> rows = new LinkedHashMap<>();
        if (Files.notExists(BASELINE))
            return rows;

        for (String line : Files.readAllLines(BASELINE)) {
            if (line.isBlank() || line.startsWith("position")) continue;   // skip header
            String[] f = line.split(",");
            rows.put(f[0], new Baseline(Integer.parseInt(f[1]), Long.parseLong(f[2]), Long.parseLong(f[3])));
        }
        return rows;
    }

    public static String gitSha() {
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
