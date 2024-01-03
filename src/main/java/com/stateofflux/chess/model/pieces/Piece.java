package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.PlayerColor;

public enum Piece {
    // White pieces are designated using upper-case letters (“PNBRQK”) while black pieces use lowercase (“pnbrqk”).
    WHITE_KING   (0,  PlayerColor.WHITE, 'K'),
    WHITE_QUEEN  (1,  PlayerColor.WHITE, 'Q'),
    WHITE_ROOK   (2,  PlayerColor.WHITE, 'R'),
    WHITE_BISHOP (3,  PlayerColor.WHITE, 'B'),
    WHITE_KNIGHT (4,  PlayerColor.WHITE, 'N'),
    WHITE_PAWN   (5,  PlayerColor.WHITE, 'P'),
    BLACK_KING   (6,  PlayerColor.BLACK, 'k'),
    BLACK_QUEEN  (7,  PlayerColor.BLACK, 'q'),
    BLACK_ROOK   (8,  PlayerColor.BLACK, 'r'),
    BLACK_BISHOP (9,  PlayerColor.BLACK, 'b'),
    BLACK_KNIGHT (10, PlayerColor.BLACK, 'n'),
    BLACK_PAWN   (11, PlayerColor.BLACK, 'p'),
    EMPTY        (12, PlayerColor.NONE,  ' ');

    public final static char KING_ALEGBRAIC = 'K';
    public final static char QUEEN_ALEGBRAIC = 'Q';
    public final static char ROOK_ALEGBRAIC = 'R';
    public final static char BISHOP_ALEGBRAIC = 'B';
    public final static char KNIGHT_ALEGBRAIC = 'N';

    private final int index; // TODO: can this be replaced by .ordinal()?
    private final PlayerColor color;
    private final char pieceChar;

    public static final int SIZE;

    static {
        SIZE = values().length;
    }

    Piece(int index, PlayerColor color, char pieceChar) {
        this.index = index;
        this.color = color;
        this.pieceChar = pieceChar;
    }

    public int getIndex() { return this.index; }
    public PlayerColor getColor() { return this.color; }
    public char getPieceChar() { return this.pieceChar; }

    public static Piece getPieceByIndex(int index) {
        // TODO: pregenerate the lookup.
        for (Piece piece : Piece.values()) {
            if (piece.getIndex() == index) {
                return piece;
            }
        }
        return EMPTY;
    }

    public static int getPieceIndexByPieceChar(String c) {
        for (Piece piece : Piece.values()) {
            if (piece.getPieceChar() == c.charAt(0)) {
                return piece.getIndex();
            }
        }

        throw new IllegalArgumentException("piece character not found: " + c);
    }

    public static Piece getPieceByAlgebraicPieceChar(char algebraicPiece, PlayerColor color) {
        if(color == PlayerColor.BLACK) {
            return switch(algebraicPiece) {
                case KING_ALEGBRAIC -> BLACK_KING;
                case QUEEN_ALEGBRAIC -> BLACK_QUEEN;
                case BISHOP_ALEGBRAIC -> BLACK_BISHOP;
                case KNIGHT_ALEGBRAIC -> BLACK_KNIGHT;
                case ROOK_ALEGBRAIC -> BLACK_ROOK;
                default -> BLACK_PAWN;
            };
        }

        if(color == PlayerColor.WHITE) {
            return switch(algebraicPiece) {
                case KING_ALEGBRAIC -> WHITE_KING;
                case QUEEN_ALEGBRAIC -> WHITE_QUEEN;
                case BISHOP_ALEGBRAIC -> WHITE_BISHOP;
                case KNIGHT_ALEGBRAIC -> WHITE_KNIGHT;
                case ROOK_ALEGBRAIC -> WHITE_ROOK;
                default -> WHITE_PAWN;
            };
        }

        throw new IllegalArgumentException("piece character not found: " + algebraicPiece);
    }

    public static Piece getPieceByPieceChar(String c) {
        for (Piece piece : Piece.values()) {
            if (piece.getPieceChar() == c.charAt(0)) {
                return piece;
            }
        }

        throw new IllegalArgumentException("piece character not found: " + c);
    }

    @Override
    public String toString() {
        return String.valueOf(pieceChar);
    }

    public PieceMovesInterface generateMoves(Board b, int location, int castlingRights, int enPassantTarget) {
        return switch (this) {
            case WHITE_KING, BLACK_KING -> new KingMoves(b, location);
            case WHITE_QUEEN, BLACK_QUEEN -> new QueenMoves(b, location);
            case WHITE_ROOK, BLACK_ROOK -> new RookMoves(b, location);
            case WHITE_BISHOP, BLACK_BISHOP -> new BishopMoves(b, location);
            case WHITE_KNIGHT, BLACK_KNIGHT -> new KnightMoves(b, location);
            case WHITE_PAWN, BLACK_PAWN -> new PawnMoves(b, location, enPassantTarget);
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isBlack() {
        return switch (this) {
            case BLACK_KING, BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT, BLACK_PAWN -> true;
            default -> false;
        };
    }

    public boolean isWhite() {
        return switch (this) {
            case WHITE_KING, WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT, WHITE_PAWN -> true;
            default -> false;
        };
    }

    public boolean isPawn() {
        return switch (this) {
            case BLACK_PAWN, WHITE_PAWN -> true;
            default -> false;
        };
    }
}
