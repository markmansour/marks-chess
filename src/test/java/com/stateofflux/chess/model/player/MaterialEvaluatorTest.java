package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class MaterialEvaluatorTest {
    @Test
    public void basicEvaluatorTest() {
        Game game = new Game();
        Evaluator e = new MaterialEvaluator();
        int score = e.evaluate(game, 0);
        assertThat(score).isZero();

        game.move("a4");
        score = e.evaluate(game, 0);
        assertThat(score).isZero(); // no material change
    }
}