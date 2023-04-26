package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class BishopMoves extends StraightLineMoves {

    public BishopMoves(Board board, int location) {
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