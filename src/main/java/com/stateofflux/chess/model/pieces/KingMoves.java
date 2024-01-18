package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.PlayerColor;

public class KingMoves implements PieceMovesInterface {

    public static final long[] KING_MOVES = new long[64];
    public static final long WHITE_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 1) | (1L << 2) | (1L << 3);
    public static final long WHITE_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 5) | (1L << 6);
    public static final long BLACK_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 61) | (1L << 62);
    public static final long BLACK_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 58) | (1L << 59);

    static {
        initializeKingAttacks();
    }

    private static void initializeKingAttacks() {
        for (int i = 0; i < 64; i++) {
            long start = 1L << i;

            long attackBb = (((start << 7L) | (start >>> 9L) | (start >>> 1L)) & (~Board.FILE_H)) |
                (((start << 9L) | (start >>> 7L) | (start << 1L)) & (~Board.FILE_A)) |
                ((start >>> 8L) | (start << 8L));

            KING_MOVES[i] = attackBb;
        }
    }

    private final Board board;
    private final int location;
    private final boolean isWhite;
    private final long occupiedBoard;
    private final int castlingRights;

    private long nonCaptureMoves;
    private long captureMoves;

    public KingMoves(Board board, int location) {
        this.board = board;
        this.location = location;
        this.isWhite = (((1L << location) & board.getWhite()) != 0);
        this.castlingRights = this.board.getCastlingRights();
        this.occupiedBoard = this.board.getOccupied();

        findCaptureAndNonCaptureMoves();
    }

    public long getCaptureMoves() {
        return this.captureMoves;
    }

    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }

    public void findCaptureAndNonCaptureMoves() {
        long opponentBoard = isWhite ? board.getBlack() : board.getWhite();

        nonCaptureMoves |= KING_MOVES[this.location] & ~occupiedBoard;
        captureMoves |= KING_MOVES[this.location] & opponentBoard;

        addCastlingMoves();
    }

    /*
     * Neither the king nor the rook has previously moved.
     * There are no pieces between the king and the rook.
     * The king is not currently in check.
     * The king does not pass through or finish on a square that is attacked by an enemy piece.
     */
    protected void addCastlingMoves() {
        if(this.castlingRights == 0)
            return;

        // king side - white
        if( (this.castlingRights & CastlingHelper.CASTLING_WHITE_KING_SIDE) != 0 &&   // castling rights remain
            (occupiedBoard & WHITE_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD) == 0 &&   // no pieces are blocking the castle
            !board.locationUnderAttack(PlayerColor.BLACK, 4) &&               // the king is not under attack
            !board.locationUnderAttack(PlayerColor.BLACK, 5) &&
            !board.locationUnderAttack(PlayerColor.BLACK, 6)                  // the king does not pass through a square that is attacked
        ) {
            this.nonCaptureMoves |= (1L << 6);  // king can castle
        }

        // queen side - white
        if( (this.castlingRights & CastlingHelper.CASTLING_WHITE_QUEEN_SIDE) != 0 &&   // castling rights remain
            (occupiedBoard & WHITE_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD) == 0 &&   // no pieces are blocking the castle
            !board.locationUnderAttack(PlayerColor.BLACK, 4) &&                // the king is not under attack
            !board.locationUnderAttack(PlayerColor.BLACK, 3) &&
            !board.locationUnderAttack(PlayerColor.BLACK, 2)                  // the king does not pass through a square that is attacked
        ) {
            this.nonCaptureMoves |= (1L << 2);  // king can castle
        }

        // king side - black
        if( (this.castlingRights & CastlingHelper.CASTLING_BLACK_KING_SIDE) != 0 &&   // castling rights remain
            (occupiedBoard & BLACK_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD) == 0 &&   // no pieces are blocking the castle
            !board.locationUnderAttack(PlayerColor.WHITE, 60) &&              // the king is not under attack
            !board.locationUnderAttack(PlayerColor.WHITE, 61) &&
            !board.locationUnderAttack(PlayerColor.WHITE, 62)                 // the king does not pass through a square that is attacked
        ) {
            this.nonCaptureMoves |= (1L << 62);  // king can castle
        }

        // queen side - black
        if( (this.castlingRights & CastlingHelper.CASTLING_BLACK_QUEEN_SIDE) != 0 &&   // castling rights remain
            (occupiedBoard & BLACK_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD) == 0 &&   // no pieces are blocking the castle
            !board.locationUnderAttack(PlayerColor.WHITE, 60) &&               // the king is not under attack
            !board.locationUnderAttack(PlayerColor.WHITE, 59) &&
            !board.locationUnderAttack(PlayerColor.WHITE, 58)                  // the king does not pass through a square that is attacked
        ) {
            this.nonCaptureMoves |= (1L << 58);  // king can castle
        }
    }

}