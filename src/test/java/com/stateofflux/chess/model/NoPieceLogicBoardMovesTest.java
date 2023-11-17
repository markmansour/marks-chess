package com.stateofflux.chess.model;

import org.testng.annotations.Test;

import com.stateofflux.chess.model.pieces.PieceMoves;
import com.stateofflux.chess.model.pieces.NoPieceLogicBoardMoves;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoPieceLogicBoardMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoPieceLogicBoardMovesTest.class);
    private static final Direction[] ALL_DIRECTIONS = new Direction[] {
            Direction.UP_LEFT,
            Direction.UP,
            Direction.UP_RIGHT,
            Direction.RIGHT,
            Direction.DOWN_RIGHT,
            Direction.DOWN,
            Direction.DOWN_LEFT,
            Direction.LEFT
    };

    @Test
    public void movingUpIntoEmptySpace() {
        Board openingBoard = getOpeningBoard();

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        NoPieceLogicBoardMoves bm = new NoPieceLogicBoardMoves(openingBoard, 8);
        bm.setMax(2);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 16 | 1L << 24 |
                        1L << 17 | 1L << 26);// both 1 & 2 squares forward
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfOpponent() {
        Board openingBoard = new Board();
        NoPieceLogicBoardMoves bm = new NoPieceLogicBoardMoves(openingBoard, 8);
        bm.setMax(5);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | // up
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 // up_right
        );
    }

    @Test
    public void movingUpIntoOccupiedSpaceOfSelf() {
        // occupied by my own piece
        Board openingBoard = new Board();
        NoPieceLogicBoardMoves bm = new NoPieceLogicBoardMoves(openingBoard, 8);
        bm.setMax(5);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | // up
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 // up_right
        );
    }

    @Test
    public void movingUpStoppingAtEndOfBoard() {
        Board openingBoard = new Board();

        openingBoard.remove(48);
        openingBoard.remove(56);
        openingBoard.remove(0); // check for wrap around and going backwards

        NoPieceLogicBoardMoves bm = new NoPieceLogicBoardMoves(openingBoard, 8);
        bm.setMax(10);  // test an invalid max

        assertThat(bm.getNonCaptureMoves()).isEqualTo(
                1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56 |
                        1L << 17 | 1L << 26 | 1L << 35 | 1L << 44 | // up_right
                        1L << 0 // down
        );
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 53);
    }

    // -------------------- maxStepsToBoundary --------------------
    @Test
    public void maxStepsToBoundary() {
        assertThat(PieceMoves.maxStepsToBoundary(0, Direction.UP)).isEqualTo(7);
        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.UP)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(56, Direction.UP)).isZero();

        assertThat(PieceMoves.maxStepsToBoundary(0, Direction.DOWN)).isZero();
        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.DOWN)).isEqualTo(1);
        assertThat(PieceMoves.maxStepsToBoundary(56, Direction.DOWN)).isEqualTo(7);

        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.RIGHT)).isEqualTo(7);
        assertThat(PieceMoves.maxStepsToBoundary(9, Direction.RIGHT)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(15, Direction.RIGHT)).isZero();

        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.LEFT)).isZero();
        assertThat(PieceMoves.maxStepsToBoundary(9, Direction.LEFT)).isEqualTo(1);
        assertThat(PieceMoves.maxStepsToBoundary(15, Direction.LEFT)).isEqualTo(7);

        assertThat(PieceMoves.maxStepsToBoundary(0, Direction.UP_RIGHT)).isEqualTo(7);
        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(9, Direction.UP_RIGHT)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(10, Direction.UP_RIGHT)).isEqualTo(5);
        assertThat(PieceMoves.maxStepsToBoundary(56, Direction.UP_RIGHT)).isZero();

        assertThat(PieceMoves.maxStepsToBoundary(0, Direction.UP_LEFT)).isZero();
        assertThat(PieceMoves.maxStepsToBoundary(15, Direction.UP_LEFT)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(9, Direction.UP_LEFT)).isEqualTo(1);

        assertThat(PieceMoves.maxStepsToBoundary(8, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(PieceMoves.maxStepsToBoundary(9, Direction.DOWN_RIGHT)).isEqualTo(1);
        assertThat(PieceMoves.maxStepsToBoundary(48, Direction.DOWN_RIGHT)).isEqualTo(6);

        assertThat(PieceMoves.maxStepsToBoundary(63, Direction.DOWN_LEFT)).isEqualTo(7);
        assertThat(PieceMoves.maxStepsToBoundary(62, Direction.DOWN_LEFT)).isEqualTo(6);
        assertThat(PieceMoves.maxStepsToBoundary(24, Direction.DOWN_LEFT)).isZero();

    }

    // -------------------- Utilities --------------------
    private Board getOpeningBoard() {
        return new Board();
    }

}
