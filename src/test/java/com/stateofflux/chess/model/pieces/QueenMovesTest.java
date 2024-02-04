package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("UnitTest")
public class QueenMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueenMovesTest.class);

    @Test
    public void attemptsToMoveWhenTrapped() {
        Board openingBoard = new Board(); // default board
        PieceMoves bm = new QueenMoves(openingBoard, 3);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        // Move white queen to 26 (c4)
        Board openingBoard = new Board("rnbqkbnr/pppppppp/8/8/2Q5/8/PPPPPPPP/RNB1KBNR", PlayerColor.WHITE);
        PieceMoves bm = new QueenMoves(openingBoard, 26);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 33 | 1L << 40 | // UP_LEFT
                        1L << 34 | 1L << 42 | // UP
                        1L << 35 | 1L << 44 | // UP_RIGHT
                        1L << 27 | 1L << 28 | 1L << 29 | 1L << 30 | 1L << 31 | // RIGHT
                        1L << 17 | // DOWN_LEFT
                        1L << 18 | // DOWN
                        1L << 19 | // DOWN_RIGHT
                        1L << 24 | 1L << 25 // LEFT
                );
        assertThat(bm.getCaptureMoves())
                .isEqualTo(1L << 50 | 1L << 53);

    }

    @Test
    public void doesNotMoveThroughPieces() {
        // Empty slots are a3 (2), a4 (3)
        // d8 (59)
        // rnb1kbnr/pppppppp/8/8/2Q5/8/PPPPPPPP/RN2KBNR b KQkq -
        Board openingBoard = new Board("rnb1kbnr/pppppppp/8/8/2Q5/8/PPPPPPPP/RN2KBNR", PlayerColor.WHITE);

        PieceMoves bm = new QueenMoves(openingBoard, 26);

        assertThat(bm.getNonCaptureMoves())
                .isEqualTo(1L << 33 | 1L << 40 | // UP_LEFT
                        1L << 34 | 1L << 42 | // UP
                        1L << 35 | 1L << 44 | // UP_RIGHT
                        1L << 27 | 1L << 28 | 1L << 29 | 1L << 30 | 1L << 31 | // RIGHT
                        1L << 17 | // DOWN_LEFT
                        1L << 18 | // DOWN
                        1L << 19 | // DOWN_RIGHT
                        1L << 24 | 1L << 25 // LEFT
                );
        assertThat(bm.getCaptureMoves())
                .isEqualTo(1L << 50 | 1L << 53);
    }

    @Test void doesNotTakeThroughOpponentsPieces() {
        // rnb1kbnr/pp1ppppp/8/2p5/2Q5/8/PPPPPPPP/RN2KBNR w KQkq -
        Board board = new Board("rnb1kbnr/pp1ppppp/8/2p5/2Q5/8/PPPPPPPP/RN2KBNR", PlayerColor.WHITE);
        PieceMoves bm = new QueenMoves(board, 26);
        int[] nonCapturePositions = Board.bitboardToArray(bm.getNonCaptureMoves());
        // should not contain 42, 50, 58!
        int[] expected = new int[]{17, 18, 19, 24, 25, 27, 28, 29, 30, 31, 33, 35, 40, 44};
        assertThat(nonCapturePositions).containsExactly(expected);
    }}
