package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class RandomEvaluationPlayerTest {
    @Test public void randomVsRandom() {
        Game game = new Game();
        Evaluator evaluator = new MaterialEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new RandomMovePlayer(PlayerColor.BLACK, evaluator);

        game.play(one, two);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }

    @Disabled(value = "Cannot get stdin when running unit tests.  Used for debugging.")
    @Test public void testChessAi() {
        Game game = new Game();
        Evaluator evaluator = new ChessAIEvaluator();
        Player human = new HumanPlayer(PlayerColor.WHITE, evaluator);
        Player chessAI = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        // chessAI.setSearchDepth(3);

        game.play(human, chessAI);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }

    @Disabled(value = "Takes too long to run")
    @Test public void testChessAiVsBasicNegaMax() {
        Game game = new Game();

        Evaluator chessAiEvaluator = new ChessAIEvaluator();
        Player chessAI = new AlphaBetaPlayer(PlayerColor.WHITE, chessAiEvaluator);

        Evaluator simplaEvaluator = new SimpleEvaluator();
        Player negaMax = new AlphaBetaPlayer(PlayerColor.BLACK, simplaEvaluator);

        game.play(chessAI, negaMax);
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }
}
