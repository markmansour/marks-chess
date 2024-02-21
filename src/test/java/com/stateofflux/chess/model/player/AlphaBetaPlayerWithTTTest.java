package com.stateofflux.chess.model.player;

import ch.qos.logback.classic.Level;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AlphaBetaPlayerWithTTTest {
    @BeforeEach
    public void setUp() {
        // change test logging level from DEBUG to INFO (it's too noisy for full game tests).
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }

    @Disabled
    @Test public void alphaBetaWithTTPlayers() {
        Game game = new Game();

        Evaluator evaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, evaluator);
        one.setSearchDepth(100);
        one.setIncrement(TimeUnit.SECONDS.toNanos(5));

        AlphaBetaPlayerWithTT two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
        two.setSearchDepth(100);
        two.setIncrement(TimeUnit.SECONDS.toNanos(5));

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
        assertThat(game.isCheckmated()).isTrue();

        // assert that depth 4 player wins
        assertThat(game.getActivePlayerColor().isWhite()).isTrue();  // black moved last and created the mate.
    }

}