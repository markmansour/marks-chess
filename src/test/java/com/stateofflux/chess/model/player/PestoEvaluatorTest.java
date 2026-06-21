package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class PestoEvaluatorTest {
    @Test
    public void startingPositionIsSymmetric() {
        Game game = new Game();
        Evaluator e = new PestoEvaluator();
        assertThat(e.evaluate(game, 0)).isZero();
    }

    @Test
    public void afterSymmetricOpeningMovesScoreStaysBalanced() {
        // 1. a4 a5 returns to a mirror-image position, so the score is zero again.
        Game game = new Game();
        Evaluator e = new PestoEvaluator();
        game.move("a4");
        game.move("a5");
        assertThat(e.evaluate(game, 0)).isZero();
    }

    @Test
    public void whiteUpAQueenScoresStronglyPositive() {
        // Black's queen removed; white to move. Score is from the side-to-move (white) perspective.
        Game game = new Game("rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        Evaluator e = new PestoEvaluator();
        assertThat(e.evaluate(game, 0)).isGreaterThan(800);
    }

    @Test
    public void blackUpAQueenScoresStronglyNegative() {
        // White's queen removed; white to move.
        Game game = new Game("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1");
        Evaluator e = new PestoEvaluator();
        assertThat(e.evaluate(game, 0)).isLessThan(-800);
    }

    @Test
    public void advancedWhitePawnScoresHigherThanStartingPawn() {
        // Orientation check: a white pawn one step from promotion must score far better
        // than one on its starting square. If the board orientation is flipped, the
        // near-promotion pawn would be scored as barely developed.
        Evaluator e = new PestoEvaluator();
        Game advanced = new Game("4k3/4P3/8/8/8/8/8/4K3 w - - 0 1");
        Game starting = new Game("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        assertThat(e.evaluate(advanced, 0)).isGreaterThan(e.evaluate(starting, 0));
    }
}
