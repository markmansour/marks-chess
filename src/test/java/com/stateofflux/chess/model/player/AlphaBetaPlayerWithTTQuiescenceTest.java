package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class AlphaBetaPlayerWithTTQuiescenceTest {
    @Test public void entersQuiescence() {
        Game game = new Game();
        game.moveLongNotation("e2e4");
        game.moveLongNotation("a7a6");
        // Bishop f1a6 will be a capture.

        Evaluator simpleEvaluator = new SimpleEvaluator();

        // test without quiescence
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, simpleEvaluator);
        one.setSearchDepth(1);  // force a quick quiescence search.
        one.setIncrement(TimeUnit.SECONDS.toNanos(3));
        Move move = one.getNextMove(game);
        assertThat(move.toLongSan()).isEqualTo("f1a6");

        // test with quiescence
        AlphaBetaPlayerWithTTQuiescence two = new AlphaBetaPlayerWithTTQuiescence(PlayerColor.WHITE, simpleEvaluator);
        two.setSearchDepth(1);  // force a quick quiescence search.
        two.setIncrement(TimeUnit.SECONDS.toNanos(3));
        move = two.getNextMove(game);
        assertThat(two.hasPerformedQuiescence()).isTrue();
        assertThat(move.toLongSan()).isNotEqualTo("f1a6");
    }

    @Test public void promotionTriggersQuiescence() {
        Game game = new Game("8/1Pk5/8/8/8/8/8/4K3 w - - 0 1");

        Evaluator simpleEvaluator = new SimpleEvaluator();

        // test without quiescence
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, simpleEvaluator);
        one.setSearchDepth(1);  // force a quick quiescence search.
        one.setIncrement(TimeUnit.SECONDS.toNanos(3));
        Move move = one.getNextMove(game);
        assertThat(move.toLongSan()).isEqualTo("b7b8Q");

        // test with quiescence
        AlphaBetaPlayerWithTTQuiescence two = new AlphaBetaPlayerWithTTQuiescence(PlayerColor.WHITE, simpleEvaluator);
        two.setSearchDepth(1);  // force a quick quiescence search.
        two.setIncrement(TimeUnit.SECONDS.toNanos(3));
        move = two.getNextMove(game);
        assertThat(two.hasPerformedQuiescence()).isTrue();
        assertThat(move.toLongSan()).isNotEqualTo("b7b8Q");
    }

}