package com.stateofflux.chess.perft;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT;
import com.stateofflux.chess.model.player.PestoEvaluator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Guards the search against efficiency regressions. Nodes-to-depth is deterministic and
 * machine-independent, so this can run in the normal unit suite with no flakiness. It reads the
 * committed baseline (it never writes it) and fails if the current search visits more than
 * {@code TOLERANCE} more nodes than the baseline. Improvements pass; re-run
 * {@link HashMoveOrderingBenchmark} to lock a new (lower) baseline in.
 */
@Tag("UnitTest")
public class HashMoveOrderingRegressionTest {

    private static final double TOLERANCE = 0.02;   // allow 2% before failing

    @Test public void searchDoesNotRegressAgainstBaseline() throws IOException {
        Map<String, PerfBaseline.Baseline> baseline = PerfBaseline.read();
        if (baseline.isEmpty())
            fail("No baseline found at %s. Run HashMoveOrderingBenchmark to record one.", PerfBaseline.BASELINE);

        for (PerfBaseline.Case c : PerfBaseline.CASES) {
            PerfBaseline.Baseline expected = baseline.get(c.name());
            assertThat(expected).as("baseline row for '%s'", c.name()).isNotNull();

            long actual = nodesToDepth(c);
            long limit = (long) Math.ceil(expected.nodesOn() * (1 + TOLERANCE));

            assertThat(actual)
                .as("'%s' depth %d visited %d nodes; baseline %d (limit %d at +%.0f%%). " +
                        "If this is an intentional change, re-run HashMoveOrderingBenchmark to update the baseline.",
                    c.name(), c.depth(), actual, expected.nodesOn(), limit, TOLERANCE * 100)
                .isLessThanOrEqualTo(limit);
        }
    }

    private long nodesToDepth(PerfBaseline.Case c) {
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(c.depth());
        player.setIncrement(TimeUnit.MINUTES.toNanos(10));   // no timeout: keeps the node count deterministic
        player.getNextMove(new Game(c.fen()));               // hash-move ordering on (default)
        return player.getNodesVisited();
    }
}
