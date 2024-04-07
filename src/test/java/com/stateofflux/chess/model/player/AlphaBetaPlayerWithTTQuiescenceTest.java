package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class AlphaBetaPlayerWithTTQuiescenceTest {
    @Disabled
    @Test public void entersQuiescence() {
        // Set board up so that bishop f1a6 will be a capture.
        Game game = new Game();
        game.moveLongNotation("e2e4");
        game.moveLongNotation("a7a6");

        // test without quiescence
        Evaluator simpleEvaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, simpleEvaluator);
        one.setSearchDepth(1);
        one.setIncrement(TimeUnit.SECONDS.toNanos(3));
        Move move = one.getNextMove(game);
        assertThat(move.toLongSan()).isEqualTo("f1a6");

        // test with quiescence
        AlphaBetaPlayerWithTTQuiescence two = new AlphaBetaPlayerWithTTQuiescence(PlayerColor.WHITE, simpleEvaluator);
        two.setSearchDepth(1);  // force a quick quiescence search.
        two.setIncrement(TimeUnit.SECONDS.toNanos(3));
        move = two.getNextMove(game);
        assertThat(two.hasPerformedQuiescence()).isTrue();

        // in the quiessence search, f1a6 has occurred.  Replies are: a8a6 (r), b7a6 (n), b7a6 (p)

        assertThat(move.toLongSan()).isNotEqualTo("f1a6");  // f1a6 should never be a good move.
    }

    @Disabled
    @Test public void goodQuiescenceMove() {
        // Set board up so that bishop f1a6 will be a capture.
        Game game = new Game("3nbrk1/2p1qppp/8/4N3/3P4/1B3R2/5QP1/5RK1 w - - 0 1");

        // test with quiescence
        Evaluator simpleEvaluator = new SimpleEvaluator();
        AlphaBetaPlayerWithTTQuiescence white = new AlphaBetaPlayerWithTTQuiescence(PlayerColor.WHITE, simpleEvaluator);
        white.setSearchDepth(5);  // force a quick quiescence search.
        white.setQuiescenceDepth(10);
        white.setIncrement(TimeUnit.SECONDS.toNanos(3));
        Move move = white.getNextMove(game);
        assertThat(white.hasPerformedQuiescence()).isTrue();
        assertThat(move.toLongSan()).doesNotEndWith("f7");        // it is not safe for white to capture f7.
    }

    @Test public void blunder() {
        Game game = new Game("r1b1kb1r/1p1ppp1p/pq1p2p1/n7/1PP1n3/8/P3PPPP/R1BQKBNR w KQkq -");
        // next move is white b4b5, then following move b6f2 for mate.
    }

    @Test public void blunder2() {
        Game game = new Game("r2Nkb1r/1P2p2p/pp4p1/2p2P2/6n1/8/PP1P1PPP/RNBQKB1R w KQkq -");
        // next move is white d8e6 but should have been b7a8Q
    }

    @Test public void blunder3() {
        Game game = new Game("rnbqk2r/1pppn2p/p2b1pp1/1B1Pp3/4P2P/2P2N2/P4PP1/RNBQK2R w KQkq -");
        // next move SHOULD NOT BE d1d4 (q)
    }

    @Test public void blunder4() {
        Game game = new Game("r1bqkbnr/1ppp4/4Np2/1B2p1pP/p3P3/2N4P/PPPP1P2/R1BQK2R b KQkq -");
        // next move SHOULD NOT BE a8a5, but instead should be moving d8e7 (save the queen)
    }


/*
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
*/

}