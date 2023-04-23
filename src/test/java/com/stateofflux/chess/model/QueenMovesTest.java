package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueenMovesTest extends NoPieceLoicBoardMovesTest {
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

        BoardMoves bm = new QueenMoves.Builder(openingBoard, 3).build();

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        Board openingBoard = new Board("RNB1KBNR/PPPPPPPP/8/2Q5/8/8/pppppppp/rnbqkbnr");

        BoardMoves bm = new BishopMoves.Builder(openingBoard, 26)
                .build();

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 33 | 1L << 40 | // UP_LEFT
                        1L << 35 | 1L << 44 | // UP_RIGHT
                        1L << 17 | // DOWN_LEFT
                        1L << 19 // DOWN_RIGHT
                );
        assertThat(bm.getCaptureMoves())
                .isEqualTo(1L << 53);

    }

    @Test
    public void doesNotMoveThroughPieces() {
        Board openingBoard = new Board("RN2KBNR/PPPPPPPP/8/2Q5/8/8/pppppppp/rnb1kbnr");

        BoardMoves bm = new BishopMoves.Builder(openingBoard, 26)
                .build();

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
