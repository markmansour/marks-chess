package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

public class KnightMovesTest {
    @Test
    public void testAllPotentialMovesAreFound() {
        Board openingBoard = new Board("8/8/8/3N4/8/8/8/8"); // D4 - Position 27
        BoardMoves bm = new KnightMoves(openingBoard, 27);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 44 | 1L << 37 |
                        1L << 21 | 1L << 12 |
                        1L << 10 | 1L << 17 |
                        1L << 33 | 1L << 42);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    void testAllCaptureAndNonCaptureMovesAreFound() {
        Board openingBoard = new Board("R1BQKBNR/PPPPPPPP/8/8/3N4/8/pppppppp/rnbqkbnr"); // white knight at D5 (35)
        BoardMoves bm = new KnightMoves(openingBoard, 35);

        // 35: 7, 15, 17, 10, -6, -15, -18, -10
        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 41 | 1L << 45 |
                        1L << 29 | 1L << 20 |
                        1L << 18 | 1L << 25);

        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 50 | 1L << 52); // two black pawns
    }

    @Test
    public void testThatMovesOffTheTopAndLeftOfBoardAreNotReturned() {
        Board openingBoard = new Board("8/8/8/8/8/8/8/N7"); // 56 - A8
        BoardMoves bm = new KnightMoves(openingBoard, 56);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 50 | 1L << 41);

        assertThat(bm.getCaptureMoves()).isZero(); // two black pawns
    }

    @Test
    public void testThatMovesOffTheBottomAndRightOfBoardAreNotReturned() {
        Board openingBoard = new Board("7N/8/8/8/8/8/8/8"); // 7 - H1
        BoardMoves bm = new KnightMoves(openingBoard, 7);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 13 | 1L << 22);

        assertThat(bm.getCaptureMoves()).isZero(); // two black pawns
    }

    @Test
    public void testThatMovesOffTheTopAndRightOfBoardAreNotReturned() {
        Board openingBoard = new Board("8/8/8/8/8/8/8/7N"); // 63 - H8
        BoardMoves bm = new KnightMoves(openingBoard, 63);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 53 | 1L << 46);

        assertThat(bm.getCaptureMoves()).isZero(); // two black pawns
    }

    @Test
    public void testThatMovesOffTheBottomAndLeftOfBoardAreNotReturned() {
        Board openingBoard = new Board("N7/8/8/8/8/8/8/8"); // 0 - A1
        BoardMoves bm = new KnightMoves(openingBoard, 0);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 17 | 1L << 10);

        assertThat(bm.getCaptureMoves()).isZero(); // two black pawns
    }
}
