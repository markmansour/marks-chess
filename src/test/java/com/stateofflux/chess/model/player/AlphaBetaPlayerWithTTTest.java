package com.stateofflux.chess.model.player;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.TranspositionTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AlphaBetaPlayerWithTTTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeEach
    public void setUp() {
        // change test logging level from DEBUG to INFO (it's too noisy for full game tests).
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }

    @Disabled("For debugging")
    @Test public void tryItOut() {
        Game game = new Game();
        Evaluator evaluator = new ChessAIEvaluator();

        AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        white.setSearchDepth(4);

        game.moveLongNotation("g1h3");
        game.moveLongNotation("c7c6");
        game.moveLongNotation("h3g1");
        game.moveLongNotation("d8a5");

        System.out.println(evaluator.evaluate(game, 0));
    }

    @Disabled("For debugging")
    @Test public void forDebugging() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();

        AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        // white.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
        white.setSearchDepth(2);

        AlphaBetaPlayerWithTT black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
        black.setSearchDepth(200);

        Move bestMove = white.getNextMove(game);
        logger.info("Best move is: {}", bestMove);  // this should NOT be h1g1 or a1b1
    }


    @Disabled
    @Test public void alphaBetaWithTTPlayers() {
        Game game = new Game();

        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(100);
        one.setIncrement(TimeUnit.SECONDS.toNanos(5));

        AlphaBetaPlayerWithTT two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(100);
        two.setIncrement(TimeUnit.SECONDS.toNanos(5));

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.isCheckmated()).isTrue();

        // assert that depth 4 player wins
        assertThat(game.getActivePlayerColor().isWhite()).isTrue();  // black moved last and created the mate.
    }

    @Test public void testSettingOfHashSize() {
        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        int initialHashSize = player.getTtHashSize();
        int mbToEntries = 1024 * 1024 / 8;
        assertThat(initialHashSize).isEqualTo(TranspositionTable.DEFAULT_HASH_SIZE_IN_MB * mbToEntries);
        player.setHashInMb(16);    // set hashsize to 16MB
        assertThat(player.getTtHashSize()).isEqualTo(16 * mbToEntries);
    }

    @Nested
    class Blunders {
        @Disabled("The code makes a blunder!")
        @Test public void addressBlunder() {
            Game game = new Game("2kr2nr/pp3ppp/2p5/2P1nb2/4q3/7P/PPP1BPP1/R1BQK1NR b - - 0 16");

            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);

            white.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            white.setSearchDepth(200);

            Move bestMove = black.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
            assertThat(bestMove.toLongSan()).isEqualTo("d8d1");
        }

        @Disabled("The code makes a blunder!")
        @Test public void uselessRookMove() {
            Game game = new Game("r1bqkbnr/pppppppp/8/8/3nP3/2N2N2/PPPP1PPP/R1BQKB1R b KQkq -");

            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);

            black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            black.setSearchDepth(200);

            Move bestMove = black.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
            assertThat(bestMove.toLongSan()).isNotEqualTo("a8b8");
        }

        @Test public void understandRookMove() {
            Game game = new Game("rnbqkbnr/pp2pppp/8/2pp4/8/N4N2/PPPPPPPP/R1BQKB1R w KQkq -");
            Evaluator evaluator = new SimpleEvaluator();

            AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            // white.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            white.setSearchDepth(7);

            AlphaBetaPlayerWithTT black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
            black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            black.setSearchDepth(200);

            Move bestMove = white.getNextMove(game);
            logger.info("Best move is: {}", bestMove);  // this should NOT be h1g1 or a1b1
        }
    }

}