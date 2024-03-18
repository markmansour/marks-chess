package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.CastlingHelper;
import com.stateofflux.chess.model.pieces.PawnMoves;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class MoveTest {
    private long openingWhitePawns = 65280L;
    private long openingBlackPawns = 71776119061217280L;

    @Test
    public void moveWithEnPassantMovementAndPawn() {
        Move m = new Move(Piece.BLACK_PAWN, 50, 34, false);
        long whitePawns = openingWhitePawns | (1L << 35);
        long blackPawns = openingBlackPawns;
        m.updateForEnPassant(whitePawns, blackPawns );  // place a white pawn to ensure enpassant
        assertThat(m.getEnPassantTarget()).isEqualTo(42);
    }

    @Test
    public void moveWithEnPassantMovementButNotPawn() {
        Move m = new Move(Piece.BLACK_ROOK, 50, 34, false);
        long whitePawns = openingWhitePawns | (1L << 35);
        long blackPawns = openingBlackPawns & ~(1L << 50);
        m.updateForEnPassant(whitePawns, blackPawns );  // place a white pawn to ensure enpassant
        assertThat(m.getEnPassantTarget()).isEqualTo(PawnMoves.NO_EN_PASSANT_VALUE);
    }


    @Nested
    class EncodeDecode {
        @Test
        public void simpleMove() {
            Move m = new Move(Piece.WHITE_PAWN, 8, 24, false);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

        @Test
        public void moveWithCapture() {
            Move m = new Move(Piece.BLACK_BISHOP, 23, 14, true);
            m.setCapturePiece(Piece.WHITE_PAWN);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

        @Test
        public void moveWithCastling() {
            Move m = new Move(Piece.BLACK_KING, CastlingHelper.BLACK_INITIAL_KING_LOCATION, CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_KING_LOCATION, false);
            m.setCastling(CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION, CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

        @Test
        public void moveWithEnPassant() {
            Move m = new Move(Piece.BLACK_PAWN, 50, 34, false);
            m.setEnPassant(42);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

        @Test
        public void moveWithPromotion() {
            Move m = new Move(Piece.WHITE_PAWN, 48, 56, false);
            m.setPromotion(Piece.WHITE_QUEEN);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

        @Test
        public void moveWithPromotionAndCapture() {
            Move m = new Move(Piece.WHITE_PAWN, 48, 57, true);
            m.setCapturePiece(Piece.BLACK_KNIGHT);
            m.setPromotion(Piece.WHITE_ROOK);
            Move m2 = Move.buildFrom(m.toLong());
            assertThat(m.equalsFullObject(m2)).isTrue();
            assertThat(m.toLong()).isEqualTo(m2.toLong());
        }

    }
}
