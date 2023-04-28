package com.stateofflux.chess.model;

import org.testng.annotations.Test;

import com.stateofflux.chess.model.pieces.BishopMoves;
import com.stateofflux.chess.model.pieces.BoardMoves;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BishopMovesTest extends NoPieceLoicBoardMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoPieceLoicBoardMovesTest.class);

    /*
     * @Test(expectedExceptions = IllegalArgumentException.class)
     * public void cannotBuildIfDirectionIsHorizontal() {
     * Board openingBoard = new Board(); // default board
     *
     * new BishopMoves.Builder(openingBoard, 2)
     * .moving(new Direction[] { Direction.LEFT })
     * .build();
     * }
     */

    @Test
    public void attemptsToMoveWhenTrapped() {
        Board openingBoard = new Board(); // default board

        BoardMoves bm = new BishopMoves(openingBoard, 2);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveFromOpeningPositionWithPawnOutOfTheWay() {
        Board openingBoard = new Board(); // default board
        openingBoard.move("b2", "b3"); // move pawn out of the way

        BoardMoves bm = new BishopMoves(openingBoard, 2);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 9 | 1L << 16);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        Board openingBoard = new Board("rnbqkbnr/pppppppp/8/8/2B5/8/PPPPPPPP/RN1QKBNR");

        BoardMoves bm = new BishopMoves(openingBoard, 26);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 33 | 1L << 40 | // UP_LEFT
                        1L << 35 | 1L << 44 | // UP_RIGHT
                        1L << 17 | // DOWN_LEFT
                        1L << 19 // DOWN_RIGHT
                );
        assertThat(bm.getCaptureMoves())
                .isEqualTo(1L << 53);

    }
}
