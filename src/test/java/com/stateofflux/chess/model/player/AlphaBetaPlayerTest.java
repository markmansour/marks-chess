package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.TranspositionTable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("FullGameTest")
public class AlphaBetaPlayerTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test public void alphaBetaDefeatsRandom() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        Player two = new RandomMovePlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, PlayerColor.BLACK)).isLessThanOrEqualTo(one.evaluate(game, PlayerColor.WHITE));  // ab beats random
    }

    @Test public void randomLosesToAlphaBeta() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, PlayerColor.BLACK)).isGreaterThanOrEqualTo(one.evaluate(game, PlayerColor.WHITE));  // ab beats random
    }

    @Test public void testToPlaceInCheck() {
        Game game = new Game("1n2k1n1/r2p3r/b1p1ppp1/p6p/3K4/8/8/7q b - - 0 30");
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(4);
        game.setPlayers(one, two);

        // white's turn (player one)
        // winning move is black:h1e1, white:d4c5 (their only move), black:e1b4 - checkmate
        Move move = two.getNextMove(game);
        assertThat(move.getFrom()).isEqualTo(7);  // doesn't use the evaluator, so I don't know the dest only the from.
    }

    @Test public void testFromWinningPositionForBlack() {
        Game game = new Game("1nbq2n1/r3p3/1pp1ppp1/p3k3/8/2r2p2/8/2b3K1 w - - 0 27");
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.getActivePlayer()).isEqualTo(one);  // player two played the last turn and won.  It moved to player one but they can't do anything.
    }

    @Test public void alphaBetaDepth2LosesToAlphaBetaDepth4() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(2);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(4);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.isCheckmated()).isTrue();

        // assert that depth 4 player wins
        assertThat(game.getActivePlayerColor().isWhite()).isTrue();  // black moved last and created the mate.
    }


    @Test public void checkmateInOneMoveIsSelectedOverCheckmateInThreeMoves() {
        Game game = new Game("8/1Q5R/4Q3/2QNP1B1/k1BKN3/P4Q2/2P3P1/4R3 w - - 17 60");
        Evaluator evaluator = new SimpleEvaluator();

        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(4);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(2);
        game.setPlayers(one, two);

        Move bestMove = one.getNextMove(game);
        game.move(bestMove);
        assertThat(game.isCheckmated()).isTrue();
    }

    @Test public void alphaBetaVsChessAi() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Evaluator chessAi = new ChessAIEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(4);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, chessAi);
        two.setSearchDepth(4);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, PlayerColor.BLACK)).isLessThanOrEqualTo(one.evaluate(game, PlayerColor.WHITE));  // ab beats random
    }

    @Test public void alphaBetaVsAlphaBetaWithTT() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(4);
        Player two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(10);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(((AlphaBetaPlayerWithTT) two).getTableHits()).isNotZero();
    }

    @Nested
    class AlphaBetaWithTT {
        @Test public void testSettingOfHashSize() {
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
            int initialHashSize = player.getTtHashSize();
            int mbToEntries = 1024 * 1024 / 8;
            assertThat(initialHashSize).isEqualTo(TranspositionTable.DEFAULT_HASH_SIZE_IN_MB * mbToEntries);
            player.setHashInMb(16);    // set hashsize to 16MB
            assertThat(player.getTtHashSize()).isEqualTo(16 * mbToEntries);
        }

        @Disabled
        @Test public void iterativeDeepeningTest() {
            Game game = new Game("4k3/2p5/8/8/8/8/3P4/4K3 w - - 0 1");  // simple board for testing
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
            game.setPlayers(one, two);

            int goalDepth = 8;

            for(int depth = 1; depth <= goalDepth; depth++) {
                one.setSearchDepth(depth);
                Move move = one.getNextMove(game);
            }
        }

        @Disabled
        @Test public void debugIterativeDeepening() {
            Game game = new Game("4k3/2p5/8/8/8/8/3P4/4K3 w - - 0 1");  // simple board for testing
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
            game.setPlayers(one, two);

            int goalDepth = 8;

            for(int depth = 1; depth <= goalDepth; depth++) {
                one.setSearchDepth(depth);
                Move move = one.getNextMove(game);
            }
        }

        @Disabled("This is not working!")
        @Test public void addressBlunder() {
            Game game = new Game("2kr2nr/pp3ppp/2p5/2P1nb2/4q3/7P/PPP1BPP1/R1BQK1NR b - - 0 16");
            long timeAllocatedInMillis = TimeUnit.SECONDS.toNanos(5);
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
            game.setPlayers(one, two);

            one.setIncrement(timeAllocatedInMillis);  // 1 second
            one.setSearchDepth(200);

            Move bestMove = one.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
            assertThat(bestMove.toLongSan()).isEqualTo("d8d1");
        }

        @Test public void iterativeDeepeningWithTimer() {
            Game game = new Game();  // simple board for testing
            long timeAllocatedInMillis = TimeUnit.SECONDS.toNanos(5);
            Evaluator evaluator = new SimpleEvaluator();
            AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
            Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
            game.setPlayers(one, two);

            one.setIncrement(timeAllocatedInMillis);  // 1 second
            one.setSearchDepth(200);

            Move bestMove = one.getNextMove(game);
            logger.info("Best move is: {}", bestMove);
//            long timeUsed = one.getLastIncrement();
//            assertThat(timeUsed).isLessThan(timeAllocatedInMillis);
        }
    }
}