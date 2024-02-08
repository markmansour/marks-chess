package com.stateofflux.chess.model.pieces;

public class CastlingHelper {
    public final static int WHITE_INITIAL_KING_LOCATION = 4;
    public final static int WHITE_KING_SIDE_INITIAL_ROOK_LOCATION = 7;
    public final static int WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION = 0;
    public final static int WHITE_KING_SIDE_CASTLING_KING_LOCATION = 6;
    public final static int WHITE_QUEEN_SIDE_CASTLING_KING_LOCATION = 2;
    public final static int WHITE_KING_SIDE_CASTLING_ROOK_LOCATION = 5;
    public final static int WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION = 3;
    public final static int BLACK_INITIAL_KING_LOCATION = 60;
    public final static int BLACK_KING_SIDE_INITIAL_ROOK_LOCATION = 63;
    public final static int BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION = 56;
    public final static int BLACK_KING_SIDE_CASTLING_KING_LOCATION = 62;
    public final static int BLACK_QUEEN_SIDE_CASTLING_KING_LOCATION = 58;
    public final static int BLACK_KING_SIDE_CASTLING_ROOK_LOCATION = 61;
    public final static int BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION = 59;
    public final static int CASTLING_WHITE_KING_SIDE = 1;
    public final static int CASTLING_BLACK_KING_SIDE = 2;
    public final static int CASTLING_WHITE_QUEEN_SIDE = 4;
    public final static int CASTLING_BLACK_QUEEN_SIDE = 8;

    public final static char WHITE_KING_CHAR = 'K';
    public final static char WHITE_QUEEN_CHAR = 'Q';
    public final static char BLACK_KING_CHAR = 'k';
    public final static char BLACK_QUEEN_CHAR = 'q';
    public final static char NO_CASTLING_CHAR = '-';
    public final static String NO_CASTLING_STRING = String.valueOf(NO_CASTLING_CHAR);

    private CastlingHelper() { }
}
