package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;

import static org.assertj.core.api.Assertions.*;

import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("UnitTest")
public class BishopMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BishopMovesTest.class);

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

        PieceMoves bm = new BishopMoves(openingBoard, 2);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveFromOpeningPositionWithPawnOutOfTheWay() {
        Game game = new Game();
        game.move(new Move(Piece.WHITE_PAWN, "b2", "b3", false));

        PieceMoves bm = new BishopMoves(game.getBoard(), 2);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 9 | 1L << 16);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void moveToEdgeOfBoard() {
        // rnbqkbnr/pppppppp/8/8/2B5/8/PPPPPPPP/RN1QKBNR b KQkq -
        Board openingBoard = new Board("rnbqkbnr/pppppppp/8/8/2B5/8/PPPPPPPP/RN1QKBNR");

        PieceMoves bm = new BishopMoves(openingBoard, 26);

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
