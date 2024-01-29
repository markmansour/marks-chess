package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class AlphaBetaPlayerTest {

    @Disabled(value = "Depth 4 is too slow")
    @Test public void basicAlphaBetaVsRandom() {
        Game game = new Game();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(one.evaluate(game)).isGreaterThan(two.evaluate(game));
    }

    @Disabled(value = "Depth 4 is too slow")
    @Test public void testToPlaceInCheck() {
        Game game = new Game("1n2k1n1/r2p3r/b1p1ppp1/p6p/3K4/8/8/7q w - - 0 30");
        Player one = new RandomMovePlayer(PlayerColor.WHITE);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK);
        two.setSearchDepth(8);
        game.setPlayers(one, two);

        // white's turn (player two)
        Move move = one.getNextMove(game);
        assertThat(move.getFrom()).isEqualTo(7);
    }

    @Test public void testFromWinningPosition() {
        Game game = new Game("1nbq2n1/r3p3/1pp1ppp1/p3k3/8/2r2p2/8/2b3K1 w - - 0 27");
        Player one = new RandomMovePlayer(PlayerColor.WHITE);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);
        two.setSearchDepth(6);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }

    @Disabled(value = "Depth 4 is too slow")
    @Test public void testMoveNotToMake() {
        Game game = new Game("3k4/8/8/p1QK2P1/P5P1/BP6/R2PP2P/1N4NR w - - 0 32");
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        one.setSearchDepth(6);
        Move move = one.getNextMove(game);
        assertThat(move.getTo()).isNotEqualTo(50);
    }

    @Disabled(value = "Depth 4 is too slow")
    @Test public void RandomVsAlphaBeta() {
        Game game = new Game();
        Player one = new RandomMovePlayer(PlayerColor.WHITE);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }

    @Disabled(value = "Depth 4 is too slow")
    @Test public void alphaBetaDepth2VsAlphaBetaDepth6() {
        Game game = new Game();
        Player one = new AlphaBetaPlayer(PlayerColor.WHITE);
        one.setSearchDepth(2);
        Player two = new AlphaBetaPlayer(PlayerColor.BLACK);
        two.setSearchDepth(6);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(two.evaluate(game)).isGreaterThan(one.evaluate(game));
    }

}
