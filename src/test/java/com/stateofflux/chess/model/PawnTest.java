package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PawnTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PawnTest.class);

    @Test
    public void openingMovesForWhite() {
        Board openingBoard = new Board(); // default board
        BoardMoves bm = BoardMoves.from(openingBoard, 11);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 19 | 1L << 27);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void openingMovesForBlack() {
        Board openingBoard = new Board(); // default board
        BoardMoves bm = BoardMoves.from(openingBoard, 52);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 44 | 1L << 36);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void oneCapture() {
        Board openingBoard = new Board(); // default board
        openingBoard.move(52, 36); // move black pawn from E7 to E5
        openingBoard.move(11, 27); // move white pawn from D2 to D4

        BoardMoves bm = BoardMoves.from(openingBoard, 27); // white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36); // take the black pawn at D5

        bm = BoardMoves.from(openingBoard, 36); // black pawn at E5
        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 28); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 27); // take the black pawn at E5

    }

    @Test
    public void whiteWithTwoCaptures() {
        Board openingBoard = new Board(); // default board
        openingBoard.move(52, 36); // move black pawn from E7 to E5
        openingBoard.move(50, 34); // move black pawn from C7 to C5
        openingBoard.move(11, 27); // move white pawn from D2 to D4

        BoardMoves bm = BoardMoves.from(openingBoard, 27); // white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36 | 1L << 34); // take the black pawn at C5 or E5
    }
}
