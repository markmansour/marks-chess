package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
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

    @Test public void testPickingTheBestNextMoveEndGame() {
        Game game = new Game("4k3/6R1/3Q4/3BN3/2B1P3/6B1/1PP2PPP/3R2K1 w - -");
        Player one = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player two = new RandomMovePlayer(PlayerColor.BLACK);
        game.setPlayers(one, two);

        one.setSearchDepth(1);
        assertThat(game.getActivePlayer()).isEqualTo(one);
        Move move = one.getNextMove(game);
        assertThat(move.toLongSan()).containsAnyOf("g7e7", "d6c6", "d6e6");

        one.setSearchDepth(2);
        assertThat(game.getActivePlayer()).isEqualTo(one);
        move = one.getNextMove(game);
        assertThat(move.toLongSan()).containsAnyOf("d5c6", "c4b5", "d6e7");
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
