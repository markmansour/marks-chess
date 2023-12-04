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

    private final int index;
    private final PlayerColor color;
    private final char pieceChar;
    private int enPassantTarget;
    private String castlingRights;

    public static final int SIZE;

    static {
        SIZE = values().length;
    }

    Piece(int index, PlayerColor color, char pieceChar) {
        this.index = index;
        this.color = color;
        this.pieceChar = pieceChar;
        this.enPassantTarget = PawnMoves.NO_EN_PASSANT_VALUE;
    }

    public int getEnPassantTarget() { return this.enPassantTarget; }
    protected void setEnPassantTarget(int enPassantTarget) { this.enPassantTarget = enPassantTarget; }
    public String getCastlingRights() { return this.castlingRights; }
    protected void setCastlingRights(String castlingRights) { this.castlingRights = castlingRights; }
    public int getIndex() { return this.index; }
    public PlayerColor getColor() { return this.color; }
    public char getPieceChar() { return this.pieceChar; }

    public static Piece getPieceByIndex(int index) {
        for (Piece piece : Piece.values()) {
            if (piece.getIndex() == index) {
                return piece;
            }
        }
        return EMPTY;
    }

    @Override
    public String toString() {
        return String.valueOf(pieceChar);
    }

    public PieceMoves generateMoves(Board b, int location, String castlingRights, int enPassantTarget) {
        switch(this) {
            case WHITE_KING:
            case BLACK_KING:
                return new KingMoves(b, location);
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                return new QueenMoves(b, location);
            case WHITE_ROOK:
            case BLACK_ROOK:
                return new RookMoves(b, location);
            case WHITE_BISHOP:
            case BLACK_BISHOP:
                return new BishopMoves(b, location);
            case WHITE_KNIGHT:
            case BLACK_KNIGHT:
                return new KnightMoves(b, location);
            case WHITE_PAWN:
            case BLACK_PAWN:
                return new PawnMoves(b, location, enPassantTarget);
            default:
                throw new IllegalArgumentException("Unexpected value: " + this);
        }
    }

    public boolean isEmpty() {
        return switch (this) {
            case EMPTY -> true;
            default -> false;
        };
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
