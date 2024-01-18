package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class RookMoves extends StraightLineMoves {
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