package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class RandomEvaluationPlayerTest {
    @Test public void randomVsRandom() {
        Game game = new Game();
        Player one = new RandomMovePlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }

    @Test public void testChessAi() {
        Game game = new Game();
/*
        Player one = new ChessAIPlayer(PlayerColor.WHITE);
        one.setSearchDepth(4);
        Player two = new BasicNegaMaxPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);
*/
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new ChessAIPlayer(PlayerColor.BLACK);
        two.setSearchDepth(4);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }
}
