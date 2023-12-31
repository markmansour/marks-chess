package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RookMoves extends StraightLineMoves {
    public static int ROOK_DIRECTIONS_MAX = 7;

    public static Direction[] ROOK_DIRECTIONS = new Direction[]{
        Direction.UP,
        Direction.RIGHT,
        Direction.DOWN,
        Direction.LEFT
    };


    public RookMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = ROOK_DIRECTIONS;
        this.max = ROOK_DIRECTIONS_MAX;
    }

    @Override
    protected void findCaptureAndNonCaptureMoves() {
        long rookAttacks = getRookAttacks(location, occupiedBoard);
        this.nonCaptureMoves = rookAttacks & ~occupiedBoard;
        this.captureMoves = rookAttacks & occupiedBoard & opponentBoard;
    }
}