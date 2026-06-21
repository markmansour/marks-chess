package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the search scores terminal nodes (no legal moves) as mate or
 * stalemate rather than handing them to the material evaluator.
 */
@Tag("UnitTest")
public class TerminalNodeScoringTest {

    @Test public void stalemateTerminalScoresAsZero() {
        // Black to move, no legal moves, not in check -> stalemate (a draw), even though
        // black is down a whole queen. The search must return 0, not the material score.
        Game game = new Game("k7/8/1Q6/8/8/8/8/7K b - - 0 1");
        assertThat(game.isStalemate()).isTrue();

        AlphaBetaPlayer player = new AlphaBetaPlayer(PlayerColor.BLACK, new PestoEvaluator());
        player.setSearchDepth(3);

        int score = player.alphaBeta(game, 3, Evaluator.MIN_VALUE, Evaluator.MAX_VALUE, PlayerColor.BLACK);
        assertThat(score).isZero();
    }

    @Test public void checkmateTerminalScoresAsLargeNegative() {
        // Black to move, checkmated. From the mated side's perspective the score must be a
        // near-MATE negative value, not the (much smaller) material deficit.
        Game game = new Game("k7/1Q6/2K5/8/8/8/8/8 b - - 0 1");
        assertThat(game.isCheckmated()).isTrue();

        AlphaBetaPlayer player = new AlphaBetaPlayer(PlayerColor.BLACK, new PestoEvaluator());
        player.setSearchDepth(3);

        int score = player.alphaBeta(game, 3, Evaluator.MIN_VALUE, Evaluator.MAX_VALUE, PlayerColor.BLACK);
        assertThat(score).isLessThanOrEqualTo(-(Evaluator.MATE_VALUE - player.getSearchDepth()));
    }

    @Test public void alphaBetaFindsMateInOne() {
        // Back-rank mate: Ra1-a8#.
        Game game = new Game("6k1/5ppp/8/8/8/8/8/R6K w - - 0 1");
        AlphaBetaPlayer player = new AlphaBetaPlayer(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(2);

        Move move = player.getNextMove(game);
        game.move(move);
        assertThat(game.isCheckmated()).isTrue();
    }

    @Test public void alphaBetaWithTTFindsMateInOne() {
        Game game = new Game("6k1/5ppp/8/8/8/8/8/R6K w - - 0 1");
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(3);

        Move move = player.getNextMove(game);
        game.move(move);
        assertThat(game.isCheckmated()).isTrue();
    }

    @Test public void prefersMateOverStalemate() {
        // White can deliver mate (e.g. Qg7#) or blunder into stalemate (e.g. Kf6). With
        // correct terminal scoring the engine takes the mate.
        Game game = new Game("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1");
        AlphaBetaPlayer player = new AlphaBetaPlayer(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(2);

        Move move = player.getNextMove(game);
        game.move(move);
        assertThat(game.isCheckmated()).isTrue();
    }
}
