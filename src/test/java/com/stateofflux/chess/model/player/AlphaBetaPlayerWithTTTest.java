package com.stateofflux.chess.model.player;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.TranspositionTable;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("FullGameTest")
class AlphaBetaPlayerWithTTTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeEach
    public void setUp() {
        // change test logging level from DEBUG to INFO (it's too noisy for full game tests).
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("UCI_Logger")).setLevel(Level.WARN);
    }

    @Disabled("Pesto is not guaranteed to be better")
    @Test public void simpleVsPesto() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.stateofflux.chess.alpha-beta-debugging")).setLevel(Level.WARN);  // turn off XML logging

        Game game = new Game();

        Evaluator simpleEvaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, simpleEvaluator);
        one.setSearchDepth(100);
        one.setIncrement(TimeUnit.SECONDS.toNanos(5));

        Evaluator pestoEvaluator = new PestoEvaluator();
        AlphaBetaPlayerWithTT two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, pestoEvaluator);
        two.setSearchDepth(100);
        two.setIncrement(TimeUnit.SECONDS.toNanos(5));

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.isCheckmated()).isTrue();

        // assert that depth 4 player wins
        assertThat(game.getActivePlayerColor().isWhite()).isTrue();  // black moved last and created the mate.
    }

    @Test public void testWinningAndTurnCombinations() {
        String whiteWinningWhiteTurn = "5B1k/8/3B4/3NNBQ1/2BKP3/8/PPP2P1P/R6R w - -";
        String blackWinningBlackTurn = "r6r/p1p2ppp/8/3pkb2/1qbnn3/4b3/8/K1b5 b - -";

        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        AlphaBetaPlayerWithTT black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        Game game;

        Move nextMove;
        int i;
        int max = 4;

        game = new Game(whiteWinningWhiteTurn);
        for(i = 1; i <= max; i++) {
            MDC.put("ply", String.format("whiteWinningWhiteTurn-%d", i));
            white.setSearchDepth(i);
            nextMove = white.getNextMove(game);
            assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.
        }

        game = new Game(blackWinningBlackTurn);
        for(i = 1; i <= max; i++) {
            MDC.put("ply", String.format("blackWinningBlackTurn-%d", i));
            black.setSearchDepth(i);
            nextMove = black.getNextMove(game);
            assertThat(nextMove.toLongSan()).containsAnyOf("d4c2", "b4b2");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.
        }
    }


    @Test public void botchedMove() {
        Game game = new Game("5B1k/8/3B4/3NNBQ1/2BKP3/8/PPP2P1P/R6R w - -");
        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        Move nextMove;
        int i;

        i = 1;
        MDC.put("ply", "debug1");
        white.setSearchDepth(i);
        nextMove = white.getNextMove(game);
        assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.
        i = 2;
        MDC.put("ply", "debug2");
        white.setSearchDepth(i);
        nextMove = white.getNextMove(game);
        assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.

        i = 3;
        MDC.put("ply", "debug3");
        white.setSearchDepth(i);
        nextMove = white.getNextMove(game);
        assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.

        i = 4;
        MDC.put("ply", "debug4");
        white.setSearchDepth(i);
        nextMove = white.getNextMove(game);
        assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.

        i = 5;
        MDC.put("ply", "debug5");
        white.setSearchDepth(i);
        nextMove = white.getNextMove(game);
        assertThat(nextMove.toLongSan()).containsAnyOf("e5f7", "g5g7");  // is coming back as f8g7, but e5f7 wins the game.  This works for standard AlphaBetaPlayer.
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
        white.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
        white.setSearchDepth(100);

        AlphaBetaPlayerWithTT black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
        black.setSearchDepth(200);

        Move bestMove = white.getNextMove(game);
        logger.info("Best move is: {}", bestMove);  // this should NOT be h1g1 or a1b1
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


        @Test public void transpositionTableIsUsed() {
            Game game = new Game();
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            one.setSearchDepth(4);

            one.getNextMove(game);

            assertThat(one.getTableHits()).isNotZero();
        }

    @Nested
    class Blunders {
        // no longer blunder
        @Test public void uselessRookMove() {
            Game game = new Game("r1bqkbnr/pppppppp/8/8/3nP3/2N2N2/PPPP1PPP/R1BQKB1R b KQkq -");

            Evaluator evaluator = new SimpleEvaluator();
            // AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);

            black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            black.setSearchDepth(200);

            Move bestMove = black.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
            assertThat(bestMove.toLongSan()).isNotEqualTo("a8b8");
        }
    }

}