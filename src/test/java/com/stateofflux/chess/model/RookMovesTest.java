package com.stateofflux.chess.model;

import org.testng.annotations.Test;

import com.stateofflux.chess.model.pieces.BoardMoves;
import com.stateofflux.chess.model.pieces.RookMoves;

import static org.assertj.core.api.Assertions.*;

public class RookMovesTest {
    @Test
    public void attemptsToMoveWhenTrapped() {
        Board openingBoard = new Board(); // default board
        BoardMoves bm = new RookMoves(openingBoard, 0);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        Board openingBoard = new Board("rnbqkbnr/pppppppp/8/8/R7/8/1PPPPPPP/1NBQKBNR");
        BoardMoves bm = new RookMoves(openingBoard, 24);

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
}
