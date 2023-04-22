package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardMovesTest.class);

    @Test
    public void movingUpIntoEmptySpace() {
        Board openingBoard = getOpeningBoard();

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        BoardMoves bm = new BoardMoves.Builder(openingBoard, 8)
                .moving(new Direction[] { Direction.UP })
                .max(2)
                .build();

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 16 | 1L << 24);// both 1 & 2 squares forward
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfOpponent() {
        // occupied by my own piece
        Board openingBoard = new Board();
        // starting at position 8, moving up 5 squares should give an answer
        // of 16 | 24 | 32 | 40 (but not 48).
        BoardMoves bm = new BoardMoves.Builder(openingBoard, 8)
                .moving(new Direction[] { Direction.UP })
                .max(5)
                .onlyIfEmpty()
                .build();

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 16 | 1L << 24 | 1L << 32 | 1L << 40);
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfSelf() {
        // occupied by my own piece
        Board openingBoard = new Board();
        // starting at position 8, moving up 5 squares should give an answer of 16 | 24
        // | 32 | 40 (but not 48).
        BoardMoves bm = new BoardMoves.Builder(openingBoard, 8)
                .moving(new Direction[] { Direction.RIGHT })
                .max(5)
                .onlyIfEmpty()
                .build();

        assertThat(bm.getNonCaptureMoves()).isZero();// there are no valid positions
    }

    @Test
    public void movingUpIntoOpponentSpace() {
        // occupied by opponent's piece
        Board openingBoard = new Board();
        BoardMoves bm = new BoardMoves.Builder(openingBoard, 8)
                .moving(new Direction[] { Direction.UP })
                .max(5)
                .includeTakingOpponent()
                .build();

        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 48);// there are no valid positions
    }

    @Test
    public void movingUpStoppingAtEndOfBoard() {
        Board openingBoard = new Board();
        openingBoard.removePieceFromBoard(48);
        openingBoard.removePieceFromBoard(56);
        openingBoard.removePieceFromBoard(0); // check for wrap around

        BoardMoves bm = new BoardMoves.Builder(openingBoard, 8)
                .moving(new Direction[] { Direction.UP })
                .max(10)
                .includeTakingOpponent()
                .build();

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    public void movingUpZero() {
        // invalid?
    }

    public void movingUpNegativePositions() {
        // invalid?
    }

    // -------------------- maxStepsToBoundary --------------------
    @Test
    public void maxStepsToBoundary() {
        assertThat(BoardMoves.maxStepsToBoundary(0, Direction.UP)).isEqualTo(7);
        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.UP)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(56, Direction.UP)).isZero();

        assertThat(BoardMoves.maxStepsToBoundary(0, Direction.DOWN)).isZero();
        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.DOWN)).isEqualTo(1);
        assertThat(BoardMoves.maxStepsToBoundary(56, Direction.DOWN)).isEqualTo(7);

        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.RIGHT)).isEqualTo(7);
        assertThat(BoardMoves.maxStepsToBoundary(9, Direction.RIGHT)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(15, Direction.RIGHT)).isZero();

        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.LEFT)).isZero();
        assertThat(BoardMoves.maxStepsToBoundary(9, Direction.LEFT)).isEqualTo(1);
        assertThat(BoardMoves.maxStepsToBoundary(15, Direction.LEFT)).isEqualTo(7);

        assertThat(BoardMoves.maxStepsToBoundary(0, Direction.UP_RIGHT)).isEqualTo(7);
        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(9, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(10, Direction.UP_RIGHT)).isEqualTo(5);
        assertThat(BoardMoves.maxStepsToBoundary(56, Direction.UP_RIGHT)).isZero();

        assertThat(BoardMoves.maxStepsToBoundary(0, Direction.UP_LEFT)).isZero();
        assertThat(BoardMoves.maxStepsToBoundary(15, Direction.UP_LEFT)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(9, Direction.UP_LEFT)).isEqualTo(1);

        assertThat(BoardMoves.maxStepsToBoundary(8, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(BoardMoves.maxStepsToBoundary(9, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(BoardMoves.maxStepsToBoundary(48, Direction.DOWN_RIGHT)).isEqualTo(6);

        assertThat(BoardMoves.maxStepsToBoundary(63, Direction.DOWN_LEFT)).isEqualTo(7);
        assertThat(BoardMoves.maxStepsToBoundary(62, Direction.DOWN_LEFT)).isEqualTo(6);
        assertThat(BoardMoves.maxStepsToBoundary(24, Direction.DOWN_LEFT)).isZero();

    }

    // -------------------- Utilities --------------------
    private Board getOpeningBoard() {
        return new Board();
    }

}
