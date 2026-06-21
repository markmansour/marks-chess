package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-game detection must be a per-position property, not a latched flag on a shared evaluator.
 * Otherwise a single deep search line that reaches an end game permanently switches the king
 * piece-square table for every later evaluation (and for both players, who share the instance).
 */
@Tag("UnitTest")
public class EvaluatorEndGameStateTest {

    @Test public void endGameDetectionDoesNotLeakBetweenEvaluations() {
        SimpleEvaluator e = new SimpleEvaluator();

        // Piece-rich (>= 4 a side): the king on a1 should use the midgame table.
        Game pieceRich = new Game("4k3/pppppppp/8/8/8/8/PPPPPPPP/K6R w - - 0 1");
        int before = e.evaluate(pieceRich, 0);

        // An end-game position (fewer than 4 pieces a side) latched the old evaluator.
        Game endGame = new Game("4k3/8/8/8/8/8/8/K6R w - - 0 1");
        e.evaluate(endGame, 0);

        int after = e.evaluate(pieceRich, 0);
        assertThat(after).isEqualTo(before);
    }

    @Test public void endGameAndMidgamePositionsScoreIndependently() {
        // Whatever order they are evaluated in, each position is scored on its own merits.
        SimpleEvaluator first = new SimpleEvaluator();
        SimpleEvaluator second = new SimpleEvaluator();

        Game pieceRich = new Game("4k3/pppppppp/8/8/8/8/PPPPPPPP/K6R w - - 0 1");
        Game endGame = new Game("4k3/8/8/8/8/8/8/K6R w - - 0 1");

        // Evaluate in opposite orders on two fresh evaluators; the piece-rich score must match.
        int richThenEnd = first.evaluate(pieceRich, 0);
        first.evaluate(endGame, 0);

        second.evaluate(endGame, 0);
        int endThenRich = second.evaluate(pieceRich, 0);

        assertThat(endThenRich).isEqualTo(richThenEnd);
    }
}
