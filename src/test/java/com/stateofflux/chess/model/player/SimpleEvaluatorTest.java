package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// SimpleEvaluator is symmetrics, which means if it is black's turn and black is winning, return a positive number OR
// if white is winning and it is white's turn, return a positive number.  But
@Tag("UnitTest")
public class SimpleEvaluatorTest {
    @Nested
    class WhitesTurn {
        @Test public void initialBoardShouldBeZero() {
            Game game = new Game();
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isZero();
        }

        @Test public void symmetricBoardShouldBeZero() {
            Game game = new Game("r2k4/8/8/8/8/8/8/R2K4 w - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isZero();
        }

        @Test public void checkmateShouldReturnLargeNumber() {
            Game game = new Game("5rk1/p4ppp/8/1QP5/8/8/PP3qrP/R2RK3 w - -");  // white is mated
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isEqualTo(-Evaluator.MATE_VALUE);
        }

        @Test public void whiteAdvantage() {
            Game game = new Game("r2k4/8/8/8/8/8/8/R2KQ3 w - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isGreaterThan(0);
        }

        @Test public void blackAdvantage() {
            Game game = new Game("r2kq3/8/8/8/8/8/8/R2K4 w - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isLessThan(0);
        }
    }

    @Nested
    class BlacksTurn {
        @Test public void symmetricBoardShouldBeZero() {
            Game game = new Game("r2k4/8/8/8/8/8/8/R2K4 b - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isZero();
        }

        @Test public void checkmateShouldReturnSmallNumber() {
            Game game = new Game("5k2/ppbb1Qp1/2p4p/8/2BP2Pq/8/PP3P2/1R3K2 b - -");  // black is mated.
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isEqualTo(-Evaluator.MATE_VALUE);
        }

        @Test public void whiteAdvantage() {
            Game game = new Game("r2k4/8/8/8/8/8/8/R2KQ3 b - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isLessThan(0);
        }

        @Test public void blackAdvantage() {
            Game game = new Game("r2kq3/8/8/8/8/8/8/R2K4 b - - 0 1");
            Evaluator se = new SimpleEvaluator();
            int score = se.evaluate(game, 0);
            assertThat(score).isGreaterThan(0);
        }
    }
}
