package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class RookMoves extends StraightLineMoves {

    public RookMoves(Board board, int location) {
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