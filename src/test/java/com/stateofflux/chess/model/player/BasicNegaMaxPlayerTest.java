package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("UnitTest")
public class BasicNegaMaxPlayerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNegaMaxPlayerTest.class);

    @Test public void lessMaterialHasLowerEvaluation() {
        Game game;
        BasicNegaMaxPlayer white = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK);

        // white is winning - w > b
        game = new Game("rnbqkbnr/p1pppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1"); // black missing a pawn.
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game), black.evaluate(game));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
        assertThat(white.evaluate(game)).isGreaterThan(black.evaluate(game));

        // black is winning - b > w
        game = new Game("rnbqkbnr/pppppppp/8/8/8/8/P1PPPPPP/RNBQKBNR w KQkq - 0 1"); // white missing a pawn.
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game), black.evaluate(game));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
        assertThat(black.evaluate(game)).isGreaterThan(white.evaluate(game));

        game = new Game("Bn6/2k4p/6q1/p4p1R/P1P2Kn1/4r1n1/5N2/6N1 b - -"); // black is winning.
        LOGGER.info("white ({})\t\tblack ({})", white.evaluate(game), black.evaluate(game));
        assertThat(white.evaluate(game)).isEqualTo(black.evaluate(game) * -1); // symmetric
    }

    @Test public void evaluatePicksMate() {
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        Game game = new Game("Bn6/2k4p/6q1/p4p1R/P1P2Kn1/4r1n1/5N2/6N1 b - -"); // black is winning.
        Move m = black.getNextMove(game); // next move should be mate.  g3h5.
        assertThat(m.getPiece()).isEqualTo(Piece.BLACK_KNIGHT);
        assertThat(m.toLongSan()).isEqualTo("g3h5");
    }

    @Test public void evaluateAllNodes() {
        BasicNegaMaxPlayer white = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        Game game = new Game(); // black is winning.
        white.getNextMove(game);
        assertThat(white.getNodesEvaluated()).isEqualTo(20 * 20);
        assertThat(black.getNodesEvaluated()).isEqualTo(0);
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void basicNegaMaxVsRandom() {
        Game game = new Game();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        Assertions.assertThat(game.isOver()).isTrue();
        Assertions.assertThat(one.evaluate(game)).isGreaterThan(two.evaluate(game));
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void basicNegaMaxVsBasicNegaMax() {
        Game game = new Game();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        Move m = one.getNextMove(game);
        assertThat(m).hasToString("adfasdf");
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void RandomVsBasicNegaMax() {
        Game game = new Game();
        Player one = new RandomMovePlayer(PlayerColor.WHITE);
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        Assertions.assertThat(game.isOver()).isTrue();
        Assertions.assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }

    @Disabled(value = "Don't play full game as part of unit test suite")
    @Test public void negaMaxDepth4VsnegaMaxDepth2() {
        Game game = new Game();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        one.setSearchDepth(2);
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        two.setSearchDepth(4);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        Assertions.assertThat(game.isOver()).isTrue();
        Assertions.assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }
}
