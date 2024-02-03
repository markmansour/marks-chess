package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@Tag("UnitTest")
public class AlphaBetaPlayerTest {

    @Disabled(value = "Depth 4 is too slow")
    @Test public void basicAlphaBetaVsRandom() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        Player two = new RandomMovePlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.getActivePlayer()).isEqualTo(two);  // player two's turn and they can't move.
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
        // fail("Fix this");
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
        assertThat(game.getActivePlayer()).isEqualTo(one);  // white's turn and they can't move.
    }

    @Disabled(value = "Full game")
    @Test public void RandomVsAlphaBeta() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, PlayerColor.BLACK)).isGreaterThanOrEqualTo(one.evaluate(game, PlayerColor.WHITE));
    }

    @Disabled(value = "Full game")
    @Test public void alphaBetaDepth2VsAlphaBetaDepth6() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(2);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(6);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game, PlayerColor.BLACK)).isGreaterThan(one.evaluate(game, PlayerColor.WHITE));
    }

}
