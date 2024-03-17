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

    public final static char KING_ALGEBRAIC = 'K';
    public final static char QUEEN_ALGEBRAIC = 'Q';
    public final static char ROOK_ALGEBRAIC = 'R';
    public final static char BISHOP_ALGEBRAIC = 'B';
    public final static char KNIGHT_ALGEBRAIC = 'N';
    public final static char PAWN_ALGEBRAIC = 'P';

    private final int index;
    private final PlayerColor color;
    private final char pieceChar;

    public static final int SIZE;
    public static final int[] WHITE_INDEX;
    public static final int[] BLACK_INDEX;

    static {
        SIZE = values().length;
        WHITE_INDEX = new int[] { 0, 1, 2, 3,  4,  5 };
        BLACK_INDEX = new int[] { 6, 7, 8, 9, 10, 11 };
    }

    Piece(int index, PlayerColor color, char pieceChar) {
        this.index = index;
        this.color = color;
        this.pieceChar = pieceChar;
    }

    public int getIndex() { return this.index; }
    public PlayerColor getColor() { return this.color; }
    public char getPieceChar() { return this.pieceChar; }
    public char getAlgebraicChar() { return Character.toUpperCase(this.pieceChar); }

    public static Piece getPieceByIndex(int index) {
        for (Piece piece : Piece.values()) {
            if (piece.getIndex() == index) {
                return piece;
            }
        }
        return EMPTY;
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

    public Piece inOpponentsColor() {
        switch(this) {
            case WHITE_KING  -> { return BLACK_KING  ; }
            case WHITE_QUEEN -> { return BLACK_QUEEN ; }
            case WHITE_ROOK  -> { return BLACK_ROOK  ; }
            case WHITE_BISHOP-> { return BLACK_BISHOP; }
            case WHITE_KNIGHT-> { return BLACK_KNIGHT; }
            case WHITE_PAWN  -> { return BLACK_PAWN  ; }
            case BLACK_KING  -> { return WHITE_KING  ; }
            case BLACK_QUEEN -> { return WHITE_QUEEN ; }
            case BLACK_ROOK  -> { return WHITE_ROOK  ; }
            case BLACK_BISHOP-> { return WHITE_BISHOP; }
            case BLACK_KNIGHT-> { return WHITE_KNIGHT; }
            case BLACK_PAWN  -> { return WHITE_PAWN  ; }
            default -> { return EMPTY; }
        }
    }

    public int colorOffset() {
        return isWhite() ? 0 : 1;
    }
}
