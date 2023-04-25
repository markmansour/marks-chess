package com.stateofflux.chess.model;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueenMovesTest    {
        private static final    Logger LOGGER = LoggerFactory.getLogger(NoPieceLoicBoardMovesTest.class);

        @Test
        public void attemptsToMoveWhenTrapped() {
                Board openingBoard = new Board(); // default board
                BoardMoves bm = new QueenMoves(openingBoard, 3);

                assertThat(bm.getNonCaptureMoves()).isZero();
                assertThat(bm.getCaptureMoves()).isZero();
        }

        @Test
        public void moveToEdgeOfBoard() {
                // Move white queen to 26 (c4)
                Board openingBoard = new Board("RNB1KBNR/PPPPPPPP/8/2Q5/8/8/pppppppp/rnbqkbnr");
                BoardMoves bm = new QueenMoves(openingBoard, 26);

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
                Board openingBoard = new Board("RN2KBNR/PPPPPPPP/8/2Q5/8/8/pppppppp/rnb1kbnr");

                BoardMoves bm = new QueenMoves(openingBoard, 26);

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
}
