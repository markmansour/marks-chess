package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.codehaus.plexus.logging.Logger;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomEvaluationPlayerTest {
    @Test public void randomGameDefaultDepthOnePlayerWithEvaluation() {
        Game game = new Game();
        // game.disable50MovesRule();
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();

        assertThat(game.isOver()).isTrue();
    }

    @Test public void testPickingTheBestNextMove() {
        Game game = new Game("rnb1kb1r/pp1qpp2/3p3p/2p1n3/4N3/5N2/PPPPPPPP/R1BQKBR1 w Qkq - 0 8");
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);
        one.setSearchDepth(1);

        // get next best move - knight puts king in check
        assertThat(game.getActivePlayer()).isEqualTo(one);
        Move move = one.getNextMove(game);
        assertThat(move.getPiece()).isEqualTo(Piece.WHITE_KNIGHT);
        assertThat(move.getFrom()).isEqualTo(28);
        assertThat(move.getTo()).isEqualTo(43);
    }


/*    @Test public void randomGameDefaultDepthTwoPlayersWithEvaluation() {
        Game game = new Game();
        // game.disable50MovesRule();
        Player one = new RandomEvaluationPlayer(PlayerColor.WHITE);
        Player two = new RandomEvaluationPlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        game.play();

        assertThat(game.isOver()).isTrue();
    }*/
}
