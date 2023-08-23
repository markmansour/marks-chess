package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.PlayerColor;

public class KingMoves extends StraightLineMoves {

    private final long kingSideCastlingDestinationBitBoard;
    private final long queenSideCastlingDestinationBitBoard;
    private final long kingSideCastlingEmptyCheckBitboard;
    private final long queenSideCastlingEmptyCheckBitboard;

    public enum Castling { QUEEN_SIDE, KING_SIDE };
    private final String castlingRights;
    private final PlayerColor playerColor;

    public static final int WHITE_KING_SIDE_CASTLING_DESTINATION = 6;
    public static final long WHITE_KING_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << WHITE_KING_SIDE_CASTLING_DESTINATION);
    public static final long WHITE_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 5) | (1L << 6);
    public static final int WHITE_QUEEN_SIDE_CASTLING_DESTINATION = 2;
    public static final long WHITE_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << WHITE_QUEEN_SIDE_CASTLING_DESTINATION);
    public static final long WHITE_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 1) | (1L << 2) | (1L << 3);
    public static final int BLACK_KING_SIDE_CASTLING_DESTINATION = 62;
    public static final long BLACK_KING_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << BLACK_KING_SIDE_CASTLING_DESTINATION);
    public static final long BLACK_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 61) | (1L << 62);
    public static final int BLACK_QUEEN_SIDE_CASTLING_DESTINATION = 58;
    public static final long BLACK_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << BLACK_QUEEN_SIDE_CASTLING_DESTINATION);
    public static final long BLACK_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 57) | (1L << 58) | (1L << 59);

    public KingMoves(Board board, int location) {
        super(board, location);

        this.castlingRights = this.getBoard().getGame().getCastlingRights();
        this.playerColor = this.getPiece().getColor();

        if(playerColor == PlayerColor.WHITE) {
            kingSideCastlingDestinationBitBoard = WHITE_KING_SIDE_CASTLING_DESTINATION_BITBOARD;
            queenSideCastlingDestinationBitBoard = WHITE_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD;
            kingSideCastlingEmptyCheckBitboard = WHITE_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD;
            queenSideCastlingEmptyCheckBitboard = WHITE_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD;
        } else {
            kingSideCastlingDestinationBitBoard = BLACK_KING_SIDE_CASTLING_DESTINATION_BITBOARD;
            queenSideCastlingDestinationBitBoard = BLACK_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD;
            kingSideCastlingEmptyCheckBitboard = BLACK_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD;
            queenSideCastlingEmptyCheckBitboard = BLACK_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD;
        }

        addCastlingMoves();
    }


    protected void addCastlingMoves() {
        /*
         * Neither the king nor the rook has previously moved.
         * There are no pieces between the king and the rook.
         * The king is not currently in check.
         * The king does not pass through or finish on a square that is attacked by an enemy piece.
         */
        if(castlingRights.charAt(0) == FenString.NO_CASTLING || playerColor == PlayerColor.NONE)
            return;

        if(castlingPiecesAreInOriginalPositions(Castling.KING_SIDE) &&
            noPiecesBetweenKingAndRook(Castling.KING_SIDE) &&
            !isInCheck() &&
            kingDoesNotPassThroughOrFinishOnAttackedSpace(Castling.KING_SIDE))
        {
            this.nonCaptureMoves |= kingSideCastlingDestinationBitBoard;
        }

        if(castlingPiecesAreInOriginalPositions(Castling.QUEEN_SIDE) &&
            noPiecesBetweenKingAndRook(Castling.QUEEN_SIDE) &&
            !isInCheck() &&
            kingDoesNotPassThroughOrFinishOnAttackedSpace(Castling.QUEEN_SIDE))
        {
            this.nonCaptureMoves |= queenSideCastlingDestinationBitBoard;
        }
    }

    protected boolean castlingPiecesAreInOriginalPositions(Castling side) {
        if(playerColor == PlayerColor.WHITE) {
            return (side == Castling.KING_SIDE && castlingRights.indexOf(FenString.WHITE_KING_SIDE_CASTLE) >= 0) ||
                (side == Castling.QUEEN_SIDE && castlingRights.indexOf(FenString.WHITE_QUEEN_SIDE_CASTLE) >= 0);
        }

        if(playerColor == PlayerColor.BLACK) {
            return (side == Castling.KING_SIDE && castlingRights.indexOf(FenString.BLACK_KING_SIDE_CASTLE) >= 0) ||
                (side == Castling.QUEEN_SIDE && castlingRights.indexOf(FenString.BLACK_QUEEN_SIDE_CASTLE) >= 0);
        }

        throw new IllegalArgumentException("PlayerColor must be white or black");
    }

    protected boolean noPiecesBetweenKingAndRook(Castling side) {
        if (side == Castling.KING_SIDE) {
            return (kingSideCastlingEmptyCheckBitboard & this.getBoard().getOccupiedBoard()) == 0;
        }

        // much be queen side.
        return (queenSideCastlingEmptyCheckBitboard & this.getBoard().getOccupiedBoard()) == 0;
//            // create a mask over the occupied board and only keep the empty check bitboard positions.
//            (~queenSideCastlingEmptyCheckBitboard ^ this.getBoard().getOccupiedBoard()
//                // check to see if the empty check bitboard positions are free
//                | queenSideCastlingEmptyCheckBitboard) == 0;

    }

    private boolean kingDoesNotPassThroughOrFinishOnAttackedSpace(Castling castling) {
        return true;
    }

    protected boolean isInCheck() {
        return false;
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP_LEFT,
                Direction.UP,
                Direction.UP_RIGHT,
                Direction.RIGHT,
                Direction.DOWN_RIGHT,
                Direction.DOWN,
                Direction.DOWN_LEFT,
                Direction.LEFT
        };
        this.max = 1;
    }
}