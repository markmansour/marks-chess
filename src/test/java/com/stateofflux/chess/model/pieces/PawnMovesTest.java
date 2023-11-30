package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PawnMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PawnMovesTest.class);

    @Test
    public void openingMovesForWhite() {
        Board openingBoard = new Board(); // default board
        PieceMoves bm = new PawnMoves(openingBoard, 11, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 19 | 1L << 27);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void openingMovesForBlack() {
        Board openingBoard = new Board(); // default board
        PieceMoves bm = new PawnMoves(openingBoard, 52, -1);

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 44 | 1L << 36);
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void openingMovesCannotPassthroughAnotherPiece() {
        // load this in as "rnbqkbnr/1ppppppp/4P3/8/8/P7/P1PP1PPP/RNBQKBNR b KQkq -"
        Board openingBoard = new Board("rnbqkbnr/1ppppppp/4P3/8/8/p7/PPPPP1PP/RNBQKBNR");
        PieceMoves bm = new PawnMoves(openingBoard, 52, -1);

        int[] nonCapturePositions = Board.bitboardToArray(bm.getNonCaptureMoves());
        LOGGER.info("non capture positions: {}", nonCapturePositions);

        assertThat(bm.getNonCaptureMoves()).isZero();
        assertThat(bm.getCaptureMoves()).isZero();
    }

    @Test
    public void oneCapture() {
        Board openingBoard = new Board(); // default board
        openingBoard.move(52, 36); // move black pawn from E7 to E5
        openingBoard.move(11, 27); // move white pawn from D2 to D4

        PieceMoves bm = new PawnMoves(openingBoard, 27, -1);// white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36); // take the black pawn at D5

        bm = new PawnMoves(openingBoard, 36, -1); // black pawn at E5
        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 28); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 27); // take the black pawn at E5

    }

    @Test
    public void whiteWithTwoCaptures() {
        Board openingBoard = new Board(); // default board
        openingBoard.move(52, 36); // move black pawn from E7 to E5
        openingBoard.move(50, 34); // move black pawn from C7 to C5
        openingBoard.move(11, 27); // move white pawn from D2 to D4

        PieceMoves bm = new PawnMoves(openingBoard, 27, -1); // white pawn at D4

        assertThat(bm.getNonCaptureMoves()).isEqualTo(1L << 35); // move forward
        assertThat(bm.getCaptureMoves()).isEqualTo(1L << 36 | 1L << 34); // take the black pawn at C5 or E5
    }

    @Test
    public void doesNotWrapAroundDuringCapture() {
        Board b = new Board("rnbqkbnr/1pppppp1/8/p6p/PP4PP/8/2PPPP2/RNBQKBNR");

        // black pawn at A5 (32)
        PieceMoves bm = new PawnMoves(b, 32, -1);

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
        Board b = new Board("rnbqkbnr/1ppppppp/8/8/8/p7/PPPPPPPP/RNBQKBNR");

        // black pawn at A3 (16)
        PieceMoves bm = new PawnMoves(b, 16, -1);

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
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4P/P3PP1P/RNBQKBNR");
        game.move("g6");
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pp1ppp2/6p1/1PpP4/8/2P4P/P3PP1P/RNBQKBNR");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
    }

    // history commands
    // is the game in a state of check?
    // is the game in a state of check mate?
    // is the game a draw?
    // is the game over?
    // is the game a stalemate?
    // is the game a threefold repetition?

}
