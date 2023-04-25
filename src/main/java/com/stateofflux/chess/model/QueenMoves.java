package com.stateofflux.chess.model;

public class QueenMoves extends StraightLineMoves {

    protected QueenMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP_LEFT,
                Direction.UP,
                Direction.UP_RIGHT,
                Direction.RIGHT,
                Direction.DOWN_RIGHT,
                Direction.DOWN,
                Direction.DOWN_LEFT,
                Direction.LEFT
        };
        this.max = 8;
    }
}
