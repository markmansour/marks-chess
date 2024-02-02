package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@Tag("UnitTest")
public class SimpleEvaluatorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEvaluatorTest.class);

    @Test public void minEqualsNegMagDefaultBoardWhiteToPlay() {
        Game game = new Game();
        Result r = getResult(game);

        assertThat(Math.min(r.a(), r.b())).isEqualTo(-Math.max(-r.b(),-r.a()));
        assertThat(r.a()).isEqualTo(-r.b());
    }

    @Test public void minEqualsNegMagBlackToPlay() {
        Game game = new Game("rnbqkbnr/p1pppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1"); // black missing a pawn.
        Result r = getResult(game);

        assertThat(Math.min(r.a(), r.b())).isEqualTo(-Math.max(-r.b(),-r.a()));
        assertThat(r.a()).isEqualTo(-r.b());
    }

    @Disabled(value = "Has bug and will fail")
    @Test public void evaluatePicksMate() {
        Game game = new Game("Bn6/2k4p/6q1/p4p1R/P1P2Kn1/4r1n1/5N2/6N1 b - -"); // black is winning and can mate
        Result r = getResult(game);

        assertThat(Math.min(r.a(), r.b())).isEqualTo(-Math.max(-r.b(),-r.a()));
        fail("Fix this");
        assertThat(r.a()).isEqualTo(-r.b());
    }

    private static Result getResult(Game game) {
        Evaluator simpleEvaluator = new SimpleEvaluator();
        BasicNegaMaxPlayer white = new BasicNegaMaxPlayer(PlayerColor.WHITE, simpleEvaluator);
        // white.setSearchDepth(2);
        BasicNegaMaxPlayer black = new BasicNegaMaxPlayer(PlayerColor.BLACK, simpleEvaluator);
        game.setPlayers(white, black);
        Move wm = white.getNextMove(game);
        Move bm = black.getNextMove(game);
        int a = white.getBestMoveScore();
        int b = black.getBestMoveScore();

        long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

        return new Result(a, b);
    }

    private record Result(int a, int b) {}
}
