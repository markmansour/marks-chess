package com.stateofflux.chess.perft;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT;
import com.stateofflux.chess.model.player.Evaluator;
import com.stateofflux.chess.model.player.SimpleEvaluator;
import one.profiler.AsyncProfiler;
import one.profiler.Events;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@Tag("PerformanceTest")
public class AlphaBetaPlayerWithTTPerfTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static AsyncProfiler asyncProfiler;

    @BeforeAll
    public static void setUp() {
        asyncProfiler = AsyncProfiler.getInstance();
    }

    @Disabled("Profiling")
    @Test
    public void alphaBetaWithTTPlayersProfiling() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        long startTime = System.nanoTime();
        String profile;

        try {
            Game game = new Game();

            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            one.setSearchDepth(100);
            one.setIncrement(TimeUnit.SECONDS.toNanos(1));

            AlphaBetaPlayerWithTT two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
            two.setSearchDepth(100);
            two.setIncrement(TimeUnit.SECONDS.toNanos(1));

            asyncProfiler.start(Events.CPU, 1_000_000);
            game.play(one, two);
        } finally {
            profile = asyncProfiler.dumpFlat(100);
            try {
                asyncProfiler.execute("stop,file=./profile/profile-alphaBetaWithTTPlayersProfiling-" + startTime + ".html");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.atInfo().log(profile);
            System.out.println("Printing profile");
            System.out.println(profile);
        }
    }
}
