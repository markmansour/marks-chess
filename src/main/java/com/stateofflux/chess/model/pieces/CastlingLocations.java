package com.stateofflux.chess.model.pieces;

public enum CastlingLocations {
    WHITE_INITIAL_KING_LOCATION(4),
    WHITE_KING_SIDE_INITIAL_ROOK_LOCATION(7),
    WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION(0),
    WHITE_KING_SIDE_CASTLING_KING_LOCATION(6),
    WHITE_QUEENS_SIDE_CASTLING_KING_LOCATION(2),
    WHITE_KING_SIDE_CASTLING_ROOK_LOCATION(5),
    WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION(3),
    BLACK_INITIAL_KING_LOCATION(60),
    BLACK_KING_SIDE_INITIAL_ROOK_LOCATION(63),
    BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION(56),
    BLACK_KING_SIDE_CASTLING_KING_LOCATION(62),
    BLACK_QUEENS_SIDE_CASTLING_KING_LOCATION(58),
    BLACK_KING_SIDE_CASTLING_ROOK_LOCATION(61),
    BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION(59);
    private final int location;

    CastlingLocations(int location) {
        this.location = location;
    }

    public int location() {
        return this.location;
    }
}