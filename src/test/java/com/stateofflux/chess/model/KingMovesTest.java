package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

public class KingMovesTest {
    @Test
    public void openingOptions() {
        Board openingBoard = new Board();

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        BoardMoves bm = BoardMoves.from(openingBoard, 4);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void movingUpIntoEmptySpace() {
        Board openingBoard = new Board();
        openingBoard.removePieceFromBoard(3);
        openingBoard.removePieceFromBoard(11);
        openingBoard.removePieceFromBoard(12);
        openingBoard.removePieceFromBoard(13);
        openingBoard.removePieceFromBoard(5);

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        BoardMoves bm = BoardMoves.from(openingBoard, 4);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1 << 3L | 1L << 11 | 1L << 12 | 1L << 13 | 1L << 5);
        assertThat(bm.getCaptureMoves()).isZero();
    }
}
