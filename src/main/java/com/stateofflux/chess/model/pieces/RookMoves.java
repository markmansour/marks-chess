package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class RookMoves extends StraightLineMoves {
    public static Direction[] ROOK_DIRECTIONS = new Direction[]{
        Direction.UP,
        Direction.RIGHT,
        Direction.DOWN,
        Direction.LEFT
    };
    public static int ROOK_DIRECTIONS_MAX = 7;

    public RookMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = ROOK_DIRECTIONS;
        this.max = ROOK_DIRECTIONS_MAX;
    }
}