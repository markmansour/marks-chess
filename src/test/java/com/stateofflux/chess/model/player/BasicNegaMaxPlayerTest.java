package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("UnitTest")
public class BasicNegaMaxPlayerTest {
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

    @Disabled(value = "Depth 4 is too slow")
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
