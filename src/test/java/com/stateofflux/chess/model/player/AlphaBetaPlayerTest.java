package com.stateofflux.chess.model.player;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.TranspositionTable;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("FullGameTest")
public class AlphaBetaPlayerTest {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeEach
    public void setUp() {
        // change test logging level from DEBUG to INFO (it's too noisy for full game tests).
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }

    @Test public void alphaBetaDefeatsRandom() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        Player two = new RandomMovePlayer(PlayerColor.BLACK, evaluator);

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, 0)).isLessThanOrEqualTo(one.evaluate(game, 0));  // ab beats random
    }

    @Test public void randomLosesToAlphaBeta() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, 0)).isGreaterThanOrEqualTo(one.evaluate(game, 0));  // ab beats random
    }

    @Test public void testToPlaceInCheck() {
        Game game = new Game("1n2k1n1/r2p3r/b1p1ppp1/p6p/3K4/8/8/7q b - - 0 30");
        Evaluator evaluator = new SimpleEvaluator();
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(4);

        // white's turn (player one)
        // winning move is black:h1e1, white:d4c5 (their only move), black:e1b4 - checkmate
        Move move = two.getNextMove(game);
        assertThat(move.getFrom()).isEqualTo(7);  // doesn't use the evaluator, so I don't know the dest only the from.
    }

    @Test public void testFromWinningPositionForBlack() {
        // stockfish can win in 6 ply
        Game game = new Game("1nbq2n1/r3p3/1pp1ppp1/p3k3/8/2r2p2/8/2b3K1 w - - 0 27");
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(6);

        assertThat(game.getClock()).isEqualTo(54);
        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        // assertThat(game.getClock()).isEqualTo(60);  due to my evaluator (we have multiple "best moves"), this isn't consistent.
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);  // player two played the last turn and won.  It moved to player one but they can't do anything.
    }

    @Test public void alphaBetaDepth2LosesToAlphaBetaDepth4() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(2);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(4);

        game.play(one, two);
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

        Move bestMove = one.getNextMove(game);
        game.move(bestMove);
        assertThat(game.isCheckmated()).isTrue();
    }

    @Test public void alphaBetaVsChessAi() {
        Game game = new Game();

        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(4);

        Evaluator chessAi = new ChessAIEvaluator();
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, chessAi);
        two.setSearchDepth(4);

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, 0)).isLessThanOrEqualTo(one.evaluate(game, 0));  // ab beats random
    }

    @Test public void alphaBetaVsAlphaBetaWithTT() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(4);

        one.getNextMove(game);

        assertThat(one.getTableHits()).isNotZero();
    }
}