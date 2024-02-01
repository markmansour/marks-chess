package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class RandomEvaluationPlayerTest {
    @Test public void randomVsRandom() {
        Game game = new Game();
        Evaluator evaluator = new MinusOneZeroOneEvaluator();
        Player one = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player two = new RandomMovePlayer(PlayerColor.BLACK, evaluator);
        game.setPlayers(one, two);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }

    @Disabled(value = "Cannot get stdin when running unit tests.  Used for debugging.")
    @Test public void testChessAi() {
        Game game = new Game();
        Evaluator evaluator = new SimpleEvaluator();
        Player human = new HumanPlayer(PlayerColor.WHITE, evaluator);
        Player chessAI = new ChessAIPlayer(PlayerColor.BLACK, evaluator);
        chessAI.setSearchDepth(3);
        game.setPlayers(human, chessAI);

        game.play();
        game.printOccupied();

        assertThat(game.isOver()).isTrue();
    }
}
