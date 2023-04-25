package com.stateofflux.chess.model;

public class RookMoves extends StraightLineMoves {

    protected RookMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP,
                Direction.RIGHT,
                Direction.DOWN,
                Direction.LEFT
        };
        this.max = 7;
    }
}