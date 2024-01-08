package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomEvaluationPlayerTest {
    @Test public void randomGameDepthOne() {
        Game game = new Game();
        game.disable50MovesRule();
        Player one = new RandomEvaluationPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();

        assertThat(game.isOver()).isTrue();
    }
}
