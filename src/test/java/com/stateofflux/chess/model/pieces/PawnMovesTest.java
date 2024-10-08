package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("UnitTest")
public class PawnMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PawnMovesTest.class);

    @Test
    public void openingMovesForWhite() {
        Board openingBoard = new Board(); // default board
        PieceMovesInterface bm = new PawnMoves(openingBoard, 11, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 19 | 1L << 27);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void openingMovesForBlack() {
        Board openingBoard = new Board(); // default board
        PieceMovesInterface bm = new PawnMoves(openingBoard, 52, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 44 | 1L << 36);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void openingMovesCannotPassthroughAnotherPiece() {
        // load this in as "rnbqkbnr/1ppppppp/4P3/8/8/P7/P1PP1PPP/RNBQKBNR b KQkq -"
        Board openingBoard = new Board("rnbqkbnr/1ppppppp/4P3/8/8/p7/PPPPP1PP/RNBQKBNR");
        PieceMovesInterface bm = new PawnMoves(openingBoard, 52, -1);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void oneCapture() {
        Game game = new Game();
        game.moveLongNotation("d2d4"); // move white pawn from D2 to D4
        game.moveLongNotation("e7e5"); // move black pawn from E7 to E5

        assertThat(game.asFen())
            .as("Initial board setup")
            .isEqualTo("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1");

        PieceMovesInterface bm = new PawnMoves(game.getBoard(), 27, -1);// white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36); // take the black pawn at D5

        bm = new PawnMoves(game.getBoard(), 36, -1); // black pawn at E5
        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 28); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 27); // take the black pawn at E5
    }

    @Test
    public void whiteWithTwoCaptures() {
        Game game = new Game();
        game.moveLongNotation("d2d4"); // move white pawn from D2 to D4
        game.moveLongNotation("e7e5"); // move black pawn from E7 to E5
        game.moveLongNotation("h2h3"); // move white pawn from H2 to H3
        game.moveLongNotation("c7c5"); // move black pawn from E7 to E5

        assertThat(game.asFen())
            .as("Initial board setup")
            .isEqualTo("rnbqkbnr/pp1p1ppp/8/2p1p3/3P4/7P/PPP1PPP1/RNBQKBNR w KQkq - 0 2");

        PieceMovesInterface bm = new PawnMoves(game.getBoard(), 27, -1); // white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36 | 1L << 34); // take the black pawn at C5 or E5
    }

    @Test
    public void doesNotWrapAroundDuringCapture() {
        // rnbqkbnr/1pppppp1/8/p6p/PP4PP/8/2PPPP2/RNBQKBNR w KQkq -
        Board b = new Board("rnbqkbnr/1pppppp1/8/p6p/PP4PP/8/2PPPP2/RNBQKBNR");

        // black pawn at A5 (32)
        PieceMovesInterface bm = new PawnMoves(b, 32, -1);

        assertThat(bm.getNonCaptureMoves()).isZero(); // no moves forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 25);

        // black pawn at H5 (39)
        bm = new PawnMoves(b, 39, -1);

        assertThat(bm.getNonCaptureMoves()).isZero(); // no moves forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 30);

        // white pawn at B4 (25)
        bm = new PawnMoves(b, 25, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 33);
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 32);

        // white pawn at G4 (30)
        bm = new PawnMoves(b, 30, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 38);
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 39);
    }

    @Test
    public void captureFromOriginalPosition() {
        // rnbqkbnr/1ppppppp/8/8/8/p7/PPPPPPPP/RNBQKBNR w KQkq -
        Board b = new Board("rnbqkbnr/1ppppppp/8/8/8/p7/PPPPPPPP/RNBQKBNR");

        // black pawn at A3 (16)
        PieceMovesInterface bm = new PawnMoves(b, 16, -1);

        assertThat(bm.getNonCaptureMoves()).isZero(); // no moves forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 9);
    }

    @Test
    public void legalEnpassentBuildUpState() {
        // setup for https://www.chessprogramming.org/En_passant#bugs
        // 2r3k1/1q1nbppp/r3p3/3pP3/p1pP4/P1Q2N2/1PRN1PPP/2R4K w - - 0 22 -
        // move b4
        // 2r3k1/1q1nbppp/r3p3/3pP3/pPpP4/P1Q2N2/2RN1PPP/2R4K b - b3
        Board b = new Board("2r3k1/1q1nbppp/r3p3/3pP3/pPpP4/P1Q2N2/2RN1PPP/2R4K");
        PawnMoves pm = new PawnMoves(b, 24, -1);
        assertThat(pm.getCaptureMoves()).isZero();  // both a4 and c4 could be legan enPassant with no context (-1)
        pm = new PawnMoves(b, 26, -1);
        assertThat(pm.getCaptureMoves()).isZero();  // both a4 and c4 could be legan enPassant with no context (-1)

        // update the board to understand the last pawn move - b3
        pm = new PawnMoves(b, 24, 17);
        assertThat(pm.getCaptureMoves()).isEqualTo(1L << 17);  // both a4 and c4 could be legan enPassant
        pm = new PawnMoves(b, 26, 17);
        assertThat(pm.getCaptureMoves()).isEqualTo(1L << 17);  // both a4 and c4 could be legan enPassant with no context (-1)
    }

    @Test
    public void enemyPawnDidNotAdvanceTwoSquaresOnPreviousTurn() {
        // not en passant even thought the board is set up for it.
        Game game = new Game("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4p/P3PPPP/RNBQKBNR w KQkq c6");
        game.move("gxh3");
        assertThat(game.asFen()).isEqualTo("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4P/P3PP1P/RNBQKBNR b KQkq - 1 1");
        game.move("g6");
        assertThat(game.asFen()).isEqualTo("rnbqkbnr/pp1ppp2/6p1/1PpP4/8/2P4P/P3PP1P/RNBQKBNR w KQkq - 0 2");
    }

    @Test public void whitePawnEvaluationTest() {
        Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        long whitePawns = b.getWhitePawnBoard();
        long blackPawns = b.getBlackPawnBoard();

        assertThat(PawnMoves.pawnEvaluation(whitePawns, blackPawns, true)).isEqualTo(0);
        assertThat(PawnMoves.pawnEvaluation(blackPawns, whitePawns, false)).isEqualTo(0);
    }

    @Test public void whitePawnEvaluationDoubleTest() {
        Board b = new Board("rnbqkbnr/pppppppp/8/8/8/1P6/1PPPPPPP/RNBQKBNR");
        long whitePawns = b.getWhitePawnBoard();
        long blackPawns = b.getBlackPawnBoard();

        assertThat(PawnMoves.pawnEvaluation(whitePawns, blackPawns, true)).isEqualTo(1);
        assertThat(PawnMoves.pawnEvaluation(blackPawns, whitePawns, false)).isEqualTo(0);
    }

    @Test public void whitePawnEvaluationIsolatedTest() {
        Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/P1PPPPPP/RNBQKBNR");
        long whitePawns = b.getWhitePawnBoard();
        long blackPawns = b.getBlackPawnBoard();

        assertThat(PawnMoves.pawnEvaluation(whitePawns, blackPawns, true)).isEqualTo(1);

        b = new Board("rnbqkbnr/pppppppp/8/8/8/8/P1P1PP1P/RNBQKBNR");
        whitePawns = b.getWhitePawnBoard();
        blackPawns = b.getBlackPawnBoard();
        assertThat(PawnMoves.pawnEvaluation(whitePawns, blackPawns, true)).isEqualTo(3);
        assertThat(PawnMoves.pawnEvaluation(blackPawns, whitePawns, false)).isEqualTo(0);
    }

    @Test public void whitePawnEvaluationBlockedTest() {
        Board b = new Board("rnbqkbnr/1ppppppp/8/p7/P7/8/1PPPPPPP/RNBQKBNR");
        long whitePawns = b.getWhitePawnBoard();
        long blackPawns = b.getBlackPawnBoard();

        assertThat(PawnMoves.pawnEvaluation(whitePawns, blackPawns, true)).isEqualTo(1);
        assertThat(PawnMoves.pawnEvaluation(blackPawns, whitePawns, false)).isEqualTo(1);
    }
}
