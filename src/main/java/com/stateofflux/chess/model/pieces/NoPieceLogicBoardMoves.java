package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

// This class is only for testing purposes and is not efficient (recalculate moves
// every time a setter is called)
public class NoPieceLogicBoardMoves extends StraightLineMoves {

    public NoPieceLogicBoardMoves(Board board, int location) {
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

        this.max = 7;
    }

    private void resetMoves() {
        this.captureMoves = 0L;
        this.nonCaptureMoves = 0L;
    }

    public void setMax(int max) {
        this.max = max;
        resetMoves();
        findCaptureAndNonCaptureMoves(); // recalculate moves
    }

    public void setDirections(Direction[] directions) {
        this.directions = directions;
        resetMoves();
        findCaptureAndNonCaptureMoves(); // recalculate moves
    }
}