package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("FullGameTest")
public class BasicNegaMaxPlayerTest {
/*
    @Test public void lessMaterialHasLowerEvaluation() {
        Game game;
        game = new Game("rnbqkbnr/p1pppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1"); // black missing a pawn.
        Evaluator evaluator = new SimpleEvaluator();
        BasicNegaMaxPlayer white = new BasicNegaMaxPlayer(PlayerColor.WHITE, evaluator);
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK, evaluator);

        // white is winning - w > b
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game), black.evaluate(game));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
        assertThat(white.evaluate(game)).isGreaterThan(black.evaluate(game));

        // black is winning - b > w
        game = new Game("rnbqkbnr/pppppppp/8/8/8/8/P1PPPPPP/RNBQKBNR w KQkq - 0 1"); // white missing a pawn.
        evaluator = new SimpleEvaluator();
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game), black.evaluate(game));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
        assertThat(black.evaluate(game)).isGreaterThan(white.evaluate(game));

        game = new Game("Bn6/2k4p/6q1/p4p1R/P1P2Kn1/4r1n1/5N2/6N1 b - -"); // black is winning.
        evaluator = new SimpleEvaluator();
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game, 0), black.evaluate(game, 0));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
    }
*/

    @Disabled
    @Test public void evaluatePicksMate() {
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK, new SimpleEvaluator());
        Game game = new Game("Bn6/2k4p/6q1/p4p1R/P1P2Kn1/4r1n1/5N2/6N1 b - -"); // black is winning.
        Move m = black.getNextMove(game); // next move should be mate.  g3h5.
        assertThat(m.getPiece()).isEqualTo(Piece.BLACK_KNIGHT);  // n : g3g5
        assertThat(m.toLongSan()).isEqualTo("g3h5");
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void basicNegaMaxVsRandom() {
        Game game = new Game();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE, new SimpleEvaluator());
        Player two = new RandomMovePlayer(PlayerColor.BLACK, new SimpleEvaluator());

        game.play(one, two);
        game.printOccupied();

        Assertions.assertThat(game.isOver()).isTrue();
        // Assertions.assertThat(one.evaluate(game)).isGreaterThan(two.evaluate(game));
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void RandomVsBasicNegaMax() {
        Game game = new Game();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, new SimpleEvaluator());
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK, new SimpleEvaluator());

        game.play(one, two);
        game.printOccupied();

        Assertions.assertThat(game.isOver()).isTrue();
        // Assertions.assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void depth3ShouldBeatDepth2() {
        Game game = new Game();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE, new SimpleEvaluator());
        one.setSearchDepth(2);
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK, new SimpleEvaluator());
        two.setSearchDepth(3);

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.isCheckmated() || game.isStalemate()).isTrue();
        assertThat(game.getActivePlayerColor().otherColor()).isEqualTo(PlayerColor.BLACK);
    }
}
