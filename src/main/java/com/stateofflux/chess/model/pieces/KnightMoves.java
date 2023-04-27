package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class KnightMoves extends BoardMoves {
    protected KnightMoves(Board board, int location) {
        super(board, location);
        // this is wrong
        setupPaths();
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP_LEFT,
                Direction.UP_RIGHT,
                Direction.DOWN_LEFT,
                Direction.DOWN_RIGHT
        };
    }

    @Override
    void findCaptureAndNonCaptureMoves() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findCaptureAndNonCaptureMoves'");
    }
}