package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.PlayerColor;

public class KingMoves extends StraightLineMoves {

    private static final int KING_DIRECTIONS_MAX = 1;
    private final long kingSideCastlingDestinationBitBoard;
    private final long queenSideCastlingDestinationBitBoard;
    private final long kingSideCastlingEmptyCheckBitboard;
    private final long queenSideCastlingEmptyCheckBitboard;

    public enum Castling { QUEEN_SIDE, KING_SIDE };
    private final String castlingRights;
    private final PlayerColor playerColor;

    public static final int WHITE_KING_SIDE_CASTLING_DESTINATION = 6;
    public static final int[] WHITE_KING_SIDE_CASTLING_SQUARES = {5, 6};
    public static final long WHITE_KING_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << WHITE_KING_SIDE_CASTLING_DESTINATION);
    public static final long WHITE_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 5) | (1L << 6);
    public static final int WHITE_QUEEN_SIDE_CASTLING_DESTINATION = 2;
    public static final int[] WHITE_QUEEN_SIDE_CASTLING_SQUARES = {2, 3};
    public static final long WHITE_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << WHITE_QUEEN_SIDE_CASTLING_DESTINATION);
    public static final long WHITE_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 1) | (1L << 2) | (1L << 3);
    public static final int BLACK_KING_SIDE_CASTLING_DESTINATION = 62;
    public static final int[] BLACK_KING_SIDE_CASTLING_SQUARES = {61, 62};
    public static final long BLACK_KING_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << BLACK_KING_SIDE_CASTLING_DESTINATION);
    public static final long BLACK_KING_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 61) | (1L << 62);
    public static final int BLACK_QUEEN_SIDE_CASTLING_DESTINATION = 58;
    public static final int[] BLACK_QUEEN_SIDE_CASTLING_SQUARES = {58, 59};
    public static final long BLACK_QUEEN_SIDE_CASTLING_DESTINATION_BITBOARD = (1L << BLACK_QUEEN_SIDE_CASTLING_DESTINATION);
    public static final long BLACK_QUEEN_SIDE_CASTLING_EMPTY_CHECK_BITBOARD = (1L << 57) | (1L << 58) | (1L << 59);

    protected static Direction[] directions;

    protected static void setupPaths() {
        directions = new Direction[] {
            Direction.UP_LEFT,
            Direction.UP,
            Direction.UP_RIGHT,
            Direction.RIGHT,
            Direction.DOWN_RIGHT,
            Direction.DOWN,
            Direction.DOWN_LEFT,
            Direction.LEFT
        };
        // this.max = KING_DIRECTIONS_MAX;
    }

    static {
        setupPaths();
    }

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

    public void findCaptureAndNonCaptureMoves() {
        findCaptureAndNonCaptureMovesInStraightLines();
    }

    public boolean isCheckingForCaptures() {
        return true;
    }

    public void findCaptureAndNonCaptureMovesInStraightLines() {
        // validation here - throw IllegalArgumentException with details when invalid
        int nextPosition;
        long nextPositionBit;
        int boardMax;

        // calculate the max moves
        for (Direction d : directions) {
            // check to see we're not going off the board.
            boardMax = Math.min(PieceMoves.maxStepsToBoundary(this.location, d), KING_DIRECTIONS_MAX);

            for (int i = 1; i <= boardMax; i++) {
                // calculate the next position
                nextPosition = this.location + (i * d.getDistance());
                nextPositionBit = 1L << nextPosition;

                // if the next position is empty, add it to the bitmap
                if ((this.occupiedBoard & nextPositionBit) == 0)
                    this.nonCaptureMoves |= nextPositionBit;

                // if the next position is the same color, stop
                if ((currentPlayerBoard & nextPositionBit) != 0)
                    break; // stop looking in the current direction

                // this is only used by the pawn.
                if (!this.isCheckingForCaptures()) {
                    if ((opponentBoard & nextPositionBit) != 0)
                        break;  // if the piece in front of the pawn is an opponent piece, then stop moving in this direction
                    else
                        continue;  // the next space is empty, so keep going in the same direction.
                }

                // if the next position is occupied, add it to the bitmap and stop
                if ((this.opponentBoard & nextPositionBit) != 0) {
                    this.captureMoves |= nextPositionBit;

                    break; // no more searches in this direction.
                }
            }
        }
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
            return (kingSideCastlingEmptyCheckBitboard & this.getBoard().getOccupied()) == 0;
        }

        // much be queen side.
        return (queenSideCastlingEmptyCheckBitboard & this.getBoard().getOccupied()) == 0;
//            // create a mask over the occupied board and only keep the empty check bitboard positions.
//            (~queenSideCastlingEmptyCheckBitboard ^ this.getBoard().getOccupiedBoard()
//                // check to see if the empty check bitboard positions are free
//                | queenSideCastlingEmptyCheckBitboard) == 0;

    }

    private boolean kingDoesNotPassThroughOrFinishOnAttackedSpace(Castling side) {
        int[] squaresToCheck;

        if (side == Castling.KING_SIDE && this.playerColor == PlayerColor.WHITE) {
            squaresToCheck = WHITE_KING_SIDE_CASTLING_SQUARES;
        } else if (side == Castling.QUEEN_SIDE && this.playerColor == PlayerColor.WHITE) {
            squaresToCheck = WHITE_QUEEN_SIDE_CASTLING_SQUARES;
        } else if (side == Castling.KING_SIDE && this.playerColor == PlayerColor.BLACK) {
            squaresToCheck = BLACK_KING_SIDE_CASTLING_SQUARES;
        } else {
            squaresToCheck = BLACK_QUEEN_SIDE_CASTLING_SQUARES;
        }

        for(int square: squaresToCheck) {
           if(isInCheckForCastling(square, side)) {
               return false;
           }
        }

        return true;
    }

    protected boolean isInCheck() {
        return false;
    }

    // we can assume the King is only in row 1 (for white) or row 8 (for black)
    protected boolean isInCheckForCastling(int tempLocation, Castling side) {
        if(this.playerColor == PlayerColor.WHITE) {
            // pawn
            if( (((1L << (tempLocation - 1 + 8)) |
                  (1L << (tempLocation + 1 + 8))) &
                this.getBoard().getPieceLocations(Piece.BLACK_PAWN)) != 0)
                return true;  // is in check from an opponent pawn.

            // rook
            if(whiteRookIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.BLACK_ROOK),
                    tempLocation))
                return true;

            // knight
            long knightBoard = this.getBoard().getPieceLocations(Piece.BLACK_KNIGHT);
            long knightAttacks = (1L << 11 | 1L << 12 | 1L << 15 |
                1L << 20 | 1L << 21 | 1L << 22 | 1L << 23);  // black attacking white king

            if((knightAttacks & knightBoard) != 0)
                return true;

            // bishop
            if(whiteBishopIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.BLACK_BISHOP),
                tempLocation))
                return true;

            // queen
            if(whiteQueenIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.BLACK_QUEEN),
                tempLocation))
                return true;
            // king
            if(whiteKingIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.BLACK_KING),
                tempLocation))
                return true;
        }

        if(this.playerColor == PlayerColor.BLACK) {
            // pawn
            if( (((1L << (tempLocation - 1 - 8)) |
                (1L << (tempLocation + 1 - 8))) &
                this.getBoard().getPieceLocations(Piece.WHITE_PAWN)) != 0)
                return true;  // is in check from an opponent pawn.

            // rook
            if(blackRookIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.WHITE_ROOK),
                tempLocation))
                return true;

            // knight
            long knightBoard = this.getBoard().getPieceLocations(Piece.WHITE_KNIGHT);
            long knightAttacks = (1L << 48 | 1L << 49| 1L <<  41 | 1L << 42 | 1L << 43 | 1L << 44 | 1L << 52 | 1L << 53);

            if((knightAttacks & knightBoard) != 0)
                return true;

            // bishop
            if(blackBishopIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.WHITE_BISHOP),
                tempLocation))
                return true;

            // queen
            if(blackQueenIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.WHITE_QUEEN),
                tempLocation))
                return true;

            // king
            if(blackKingIsInStraightLineAndNotBlocked(
                this.getBoard().getOccupied(),
                this.getBoard().getPieceLocations(Piece.WHITE_KING),
                tempLocation))
                return true;
        }

        return false; // this should never happen
    }

    private boolean knightCanReachKing(int kingLocation) {
        return false;
    }

    private boolean whiteQueenIsInStraightLineAndNotBlocked(long enemyBoard, long queenBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, queenBoard, tempLocation, new Direction[] {Direction.UP_RIGHT, Direction.UP, Direction.UP_LEFT}, QueenMoves.QUEEN_DIRECTIONS_MAX);
    }

    private boolean whiteKingIsInStraightLineAndNotBlocked(long enemyBoard, long kingBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, kingBoard, tempLocation, new Direction[] {Direction.UP_RIGHT, Direction.UP, Direction.UP_LEFT}, KingMoves.KING_DIRECTIONS_MAX);
    }

    private boolean whiteBishopIsInStraightLineAndNotBlocked(long enemyBoard, long bishopBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, bishopBoard, tempLocation, new Direction[] {Direction.UP_RIGHT, Direction.UP_LEFT}, BishopMoves.BISHOP_DIRECTIONS_MAX);
    }

    private boolean whiteRookIsInStraightLineAndNotBlocked(long enemyBoard, long rookBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, rookBoard, tempLocation, new Direction[] {Direction.UP}, RookMoves.ROOK_DIRECTIONS_MAX);
    }

    private boolean blackQueenIsInStraightLineAndNotBlocked(long enemyBoard, long queenBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, queenBoard, tempLocation, new Direction[] {Direction.DOWN_RIGHT, Direction.DOWN, Direction.DOWN_LEFT}, QueenMoves.QUEEN_DIRECTIONS_MAX);
    }

    private boolean blackKingIsInStraightLineAndNotBlocked(long enemyBoard, long kingBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, kingBoard, tempLocation, new Direction[] {Direction.DOWN_RIGHT, Direction.DOWN, Direction.DOWN_LEFT}, KingMoves.KING_DIRECTIONS_MAX);
    }

    private boolean blackBishopIsInStraightLineAndNotBlocked(long enemyBoard, long bishopBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, bishopBoard, tempLocation, new Direction[] {Direction.DOWN_RIGHT, Direction.DOWN_LEFT}, BishopMoves.BISHOP_DIRECTIONS_MAX);
    }

    private boolean blackRookIsInStraightLineAndNotBlocked(long enemyBoard, long rookBoard, int tempLocation) {
        return hasCaptureMovesInStraightLines(enemyBoard, rookBoard, tempLocation, new Direction[] {Direction.DOWN}, RookMoves.ROOK_DIRECTIONS_MAX);
    }

    public boolean hasCaptureMovesInStraightLines(long enemyBoard, long attackingBoardPieces, int tempLocation, Direction[] directions, int max) {
        int nextPosition;
        long nextPositionBit;
        int boardMax;

        for (Direction d: directions) {
            boardMax = Math.min(PieceMoves.maxStepsToBoundary(tempLocation, d), max);

            for (int i = 1; i <= boardMax; i++) {
                nextPosition = tempLocation + (i * d.getDistance());
                nextPositionBit = 1L << nextPosition;

                if((attackingBoardPieces & nextPositionBit) != 0)  // the attacking piece can reach the king
                    return true;

                if((enemyBoard & nextPositionBit) != 0)  // there is a non-attacking piece blocking the take
                    break;
            }
        }

        return false;
    }
}