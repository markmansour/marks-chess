package com.stateofflux.chess.model.pieces;

public class CastlingHelper {
    public static int WHITE_INITIAL_KING_LOCATION = 4;
    public static int WHITE_KING_SIDE_INITIAL_ROOK_LOCATION = 7;
    public static int WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION = 0;
    public static int WHITE_KING_SIDE_CASTLING_KING_LOCATION = 6;
    public static int WHITE_QUEEN_SIDE_CASTLING_KING_LOCATION = 2;
    public static int WHITE_KING_SIDE_CASTLING_ROOK_LOCATION = 5;
    public static int WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION = 3;
    public static int BLACK_INITIAL_KING_LOCATION = 60;
    public static int BLACK_KING_SIDE_INITIAL_ROOK_LOCATION = 63;
    public static int BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION = 56;
    public static int BLACK_KING_SIDE_CASTLING_KING_LOCATION = 62;
    public static int BLACK_QUEEN_SIDE_CASTLING_KING_LOCATION = 58;
    public static int BLACK_KING_SIDE_CASTLING_ROOK_LOCATION = 61;
    public static int BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION = 59;
    public static int CASTLING_WHITE_KING_SIDE = 1;
    public static int CASTLING_BLACK_KING_SIDE = 2;
    public static int CASTLING_WHITE_QUEEN_SIDE = 4;
    public static int CASTLING_BLACK_QUEEN_SIDE = 8;
    private final int location;

    CastlingHelper(int location) {
        this.location = location;
    }

    public int location() {
        return this.location;
    }
}
