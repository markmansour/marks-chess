package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoPieceLoicBoardMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoPieceLoicBoardMovesTest.class);

    @Test
    public void movingUpIntoEmptySpace() {
        Board openingBoard = getOpeningBoard();

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        BoardMoves bm = new NoPieceLogicBoardMoves.Builder(openingBoard, 8)
                .max(2)
                .build();

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 16 | 1L << 24 |
                        1L << 17 | 1L << 26);// both 1 & 2 squares forward
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfOpponent() {
        Board openingBoard = new Board();
        BoardMoves bm = new NoPieceLogicBoardMoves.Builder(openingBoard, 8)
                .max(5)
                .build();

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | // up
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 // up_right
        );
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfSelf() {
        // occupied by my own piece
        Board openingBoard = new Board();
        BoardMoves bm = new NoPieceLogicBoardMoves.Builder(openingBoard, 8)
                // .moving(new Direction[] { Direction.RIGHT })
                .max(5)
                .build();

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | // up
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 // up_right
        );
    }

    @Test
    public void movingUpStoppingAtEndOfBoard() {
        Board openingBoard = new Board();

        openingBoard.removePieceFromBoard(48);
        openingBoard.removePieceFromBoard(56);
        openingBoard.removePieceFromBoard(0); // check for wrap around and going backwards

        BoardMoves bm = new NoPieceLogicBoardMoves.Builder(openingBoard, 8)
                .max(10)
                .build();

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56 |
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 | // up_right
                        1L << 0 // down
        );
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 53);
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
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(0, Direction.UP)).isEqualTo(7);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.UP)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(56, Direction.UP)).isZero();

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(0, Direction.DOWN)).isZero();
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.DOWN)).isEqualTo(1);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(56, Direction.DOWN)).isEqualTo(7);

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.RIGHT)).isEqualTo(7);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(9, Direction.RIGHT)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(15, Direction.RIGHT)).isZero();

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.LEFT)).isZero();
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(9, Direction.LEFT)).isEqualTo(1);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(15, Direction.LEFT)).isEqualTo(7);

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(0, Direction.UP_RIGHT)).isEqualTo(7);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(9, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(10, Direction.UP_RIGHT)).isEqualTo(5);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(56, Direction.UP_RIGHT)).isZero();

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(0, Direction.UP_LEFT)).isZero();
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(15, Direction.UP_LEFT)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(9, Direction.UP_LEFT)).isEqualTo(1);

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(8, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(9, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(48, Direction.DOWN_RIGHT)).isEqualTo(6);

        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(63, Direction.DOWN_LEFT)).isEqualTo(7);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(62, Direction.DOWN_LEFT)).isEqualTo(6);
        assertThat(NoPieceLogicBoardMoves.maxStepsToBoundary(24, Direction.DOWN_LEFT)).isZero();

    }

    // -------------------- Utilities --------------------
    private Board getOpeningBoard() {
        return new Board();
    }

}
