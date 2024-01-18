package com.stateofflux.chess.model;

public enum Direction {
    UP(8),
    DOWN(-8),
    LEFT(-1),
    RIGHT(1),

    UP_LEFT(7),
    UP_RIGHT(9),
    DOWN_LEFT(-9),
    DOWN_RIGHT(-7);

    private final int distance;

    static final long[][] RAYS = new long[8][64];

    static {
        for(int location = 0; location < 64; location++) {
            RAYS[UP.ordinal()][location] = 0x0101010101010100L << location;
            RAYS[DOWN.ordinal()][location]= 0x0080808080808080L >> (63 - location);
            RAYS[RIGHT.ordinal()][location] = 2 * ((1L << (location | 7)) - (1L << location));
            RAYS[LEFT.ordinal()][location] = (1L << location) - (1L << (location & 56));
            RAYS[UP_LEFT.ordinal()][location] = moveBoardWest(0x102040810204000L, 7 - Board.file(location)) << (Board.rank(location) * 8);
            RAYS[UP_RIGHT.ordinal()][location] = moveBoardEast(0x8040201008040200L, Board.file(location)) << (Board.rank(location) * 8);
            RAYS[DOWN_LEFT.ordinal()][location] = moveBoardWest(0x40201008040201L, 7 - Board.file(location)) >> ((7 - Board.rank(location)) * 8);
            RAYS[DOWN_RIGHT.ordinal()][location] = moveBoardEast(0x2040810204080L, Board.file(location)) >> ((7 - Board.rank(location)) * 8);
        }
    }

    public static long getRay(Direction d, int location) {
        return RAYS[d.ordinal()][location];
    }

    /**
     * Moves all set bits in the given bitboard n squares west and returns the new
     * bitboard, discarding those that fall off the edge.
     *
     * @param board Board to move bits west on
     * @param n Number of squares to move west
     * @return A bitboard with all set bits moved one square west, bits falling off the edge discarded
     */
    static long moveBoardWest(long board, int n) {
        long newBoard = board;
        for (int i = 0; i < n; i++) {
            newBoard = ((newBoard >> 1) & (~Board.FILE_H));
        }

        return newBoard;
    }

    static long moveBoardEast(long board, int n) {
        long newBoard = board;
        for (int i = 0; i < n; i++) {
            newBoard = ((newBoard << 1) & (~Board.FILE_A));
        }

        return newBoard;
    }

    Direction(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return this.distance;
    }
}
