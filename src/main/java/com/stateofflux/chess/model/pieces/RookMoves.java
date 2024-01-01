package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RookMoves extends StraightLineMoves {
    public static int ROOK_DIRECTIONS_MAX = 7;

    public RookMoves(Board board, int location) {
        super(board, location);
    }

    @Override
    public void findCaptureAndNonCaptureMoves() {
        long rookAttacks = getRookAttacks(location, occupiedBoard);
        this.nonCaptureMoves = rookAttacks & ~occupiedBoard;
        this.captureMoves = rookAttacks & occupiedBoard & opponentBoard;
    }
}