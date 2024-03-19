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

import static junit.framework.Assert.fail;
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

    @Test void anotherIssue() {
        /*
            1476654 >ABTTdinf(3): position startpos moves e2e3 e7e5 c2c4 g7g5 d2d4 b8c6 b1c3 e5d4 e3d4 d8e7 f1e2 b7b6 c1e3 e7d6 d4d5 c6e7 c3b5 d6b4 e3d2 b4b2 d2c3 b2c3 b5c3 f8g7 c3b5 g7e5 d5d6 c7d6 b5c7 e8d8 c7a8 c8b7 a1b1 b7g2 e2f3 g2h1 f3h1
            1476654 >ABTTdinf(3): isready
            1476782 <ABTTdinf(3): readyok
            1476782 >ABTTdinf(3): go wtime 19205 btime 34591 movestogo 22
            1476785 <ABTTdinf(3): 21:05:54.954 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - readyok
            1476790 <ABTTdinf(3): info string depth set to 100; increment set to 1414ms
            1476790 <ABTTdinf(3): 21:05:54.961 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - info string depth set to 100; increment set to 1414ms
            1476798 <ABTTdinf(3): info depth 1 score -466 nodes 27 nps 4500 hashfull 0 time 5 pv e5h2
            1476798 <ABTTdinf(3): 21:05:54.966 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - info depth 1 score -466 nodes 27 nps 4500 hashfull 0 time 5 pv e5h2
            1476800 <ABTTdinf(3): info depth 2 score -582 nodes 121 nps 15125 hashfull 0 time 7 pv e5c3 e1e2
            1476800 <ABTTdinf(3): 21:05:54.968 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - info depth 2 score -582 nodes 121 nps 15125 hashfull 0 time 7 pv e5c3 e1e2
            1476824 <ABTTdinf(3): info depth 3 score -454 nodes 1661 nps 47457 hashfull 0 time 34 pv e5h2 g1f3 g8f6
            1476824 <ABTTdinf(3): 21:05:54.995 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - info depth 3 score -454 nodes 1661 nps 47457 hashfull 0 time 34 pv e5h2 g1f3 g8f6
            1477243 <ABTTdinf(3): info depth 4 score -644 nodes 17519 nps 38588 hashfull 1 time 453 pv g8h6 g1e2 e5h2 a8b6
            1477243 <ABTTdinf(3): 21:05:55.414 [com.stateofflux.chess.App.main()] INFO  UCI_Logger - info depth 4 score -644 nodes 17519 nps 38588 hashfull 1 time 453 pv g8h6 g1e2 e5h2 a8b6
            1477438 <ABTTdinf(3): [WARNING]
            1477440 <ABTTdinf(3): java.lang.AssertionError: Location not found: 7
            1477450 <ABTTdinf(3):     at com.stateofflux.chess.model.Board.getBoardIndex (Board.java:295)
            1477453 <ABTTdinf(3):     at com.stateofflux.chess.model.Board.update (Board.java:322)
            1477457 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.updateBoard (Game.java:456)
            1477460 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.move (Game.java:399)
            1477460 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.cleanUpMoves (Game.java:279)
            1477460 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.generateMovesFor (Game.java:246)
            1477460 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.generateMoves (Game.java:223)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.Game.isCheckmated (Game.java:686)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.player.PieceSquareEvaluator.evaluate (PieceSquareEvaluator.java:68)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.player.Player.evaluate (Player.java:32)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:216)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:280)
            1477461 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:280)
            1477462 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:280)
            1477462 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:280)
            1477462 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.alphaBeta (AlphaBetaPlayerWithTT.java:280)
            1477463 <ABTTdinf(3):     at com.stateofflux.chess.model.player.AlphaBetaPlayerWithTT.getNextMove (AlphaBetaPlayerWithTT.java:94)
            1477463 <ABTTdinf(3):     at com.stateofflux.chess.App.uciLoop (App.java:161)
            1477463 <ABTTdinf(3):     at com.stateofflux.chess.App.main (App.java:69)
            1477463 <ABTTdinf(3):     at org.codehaus.mojo.exec.ExecJavaMojo$1.run (ExecJavaMojo.java:254)
            1477463 <ABTTdinf(3):     at java.lang.Thread.run (Thread.java:833)

            Fen: N2k2nr/p2pnp1p/1p1p4/4b1p1/2P5/8/P4P1P/1R1QK1NB b - - 0 19
            Key: 00FFAFDACCD40C0D
         */

        Game game = new Game();
        String[] longMoves = "e2e3 e7e5 c2c4 g7g5 d2d4 b8c6 b1c3 e5d4 e3d4 d8e7 f1e2 b7b6 c1e3 e7d6 d4d5 c6e7 c3b5 d6b4 e3d2 b4b2 d2c3 b2c3 b5c3 f8g7 c3b5 g7e5 d5d6 c7d6 b5c7 e8d8 c7a8 c8b7 a1b1 b7g2 e2f3 g2h1 f3h1".split(" ");
        for(String m : longMoves) {
            logger.atDebug().log("move: {}", m);
            game.moveLongNotation(m);
        }

        assertThat(game.asFen()).startsWith("N2k2nr/p2pnp1p/1p1p4/4b1p1/2P5/8/P4P1P/1R1QK1NB b - -");
        fail("we have a bug");
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
            AlphaBetaPlayerWithTT white = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);

            black.setIncrement(TimeUnit.SECONDS.toNanos(5));  // 1 second
            black.setSearchDepth(200);

            Move bestMove = black.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
            assertThat(bestMove.toLongSan()).isNotEqualTo("a8b8");
        }
    }

}