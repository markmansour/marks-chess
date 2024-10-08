package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
public class RookMovesTest {
    @Test
    public void attemptsToMoveWhenTrapped() {
        Board openingBoard = new Board(); // default board
        PieceMoves bm = new RookMoves(openingBoard, 0);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        Board openingBoard = new Board("rnbqkbnr/pppppppp/8/8/R7/8/1PPPPPPP/1NBQKBNR");
        PieceMoves bm = new RookMoves(openingBoard, 24);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(
                        1L << 32 | 1L << 40 | // UP
                        1L << 25 | 1L << 26 | 1L << 27 | 1L << 28 | 1L << 29 | 1L << 30 | 1L << 31 | // RIGHT
                        1L << 16 | 1L << 8 | 1L << 0 | // DOWN
                        0 // LEFT
                );
        assertThat(bm.getCaptureMoves())
                .isEqualTo(1L << 48);

    }

    @Test void castleTakeCastleAffectsCastlingRights() {
        Game game = new Game("r3k2r/8/8/8/8/8/8/R3K2R w KQkq -");
        game.move("Rxa8+");
        // assertThat(game.getCastlingRights().toCharArray()).containsExactlyInAnyOrder(new char[] {'k', 'K'});
        assertThat(game.asFen()).startsWith("R3k2r/8/8/8/8/8/8/4K2R b Kk -");
        assertThat(game.isChecked()).isTrue();
        assertThat(game.generateMoves().asLongSan()).containsOnly("e8d7", "e8e7", "e8f7");
    }

    @Test void cantUseCastlingToGetOutOfCheck() {
        Game game = new Game("R3k2r/8/8/8/8/8/8/4K2R b Kk -");
        assertThat(game.isChecked()).isTrue();
        assertThat(game.generateMoves().asLongSan()).containsOnly("e8d7", "e8e7", "e8f7");
    }
}
