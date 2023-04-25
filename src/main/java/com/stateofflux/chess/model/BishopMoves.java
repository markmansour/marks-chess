package com.stateofflux.chess.model;

public class BishopMoves extends StraightLineMoves {

    protected BishopMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP_LEFT,
                Direction.UP_RIGHT,
                Direction.DOWN_LEFT,
                Direction.DOWN_RIGHT
        };
        this.max = 7;
    }
}