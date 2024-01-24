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
}
