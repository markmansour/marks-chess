package com.stateofflux.chess.model;

public enum Piece {
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
}
