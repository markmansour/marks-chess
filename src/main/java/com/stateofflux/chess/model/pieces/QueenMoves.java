package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class QueenMoves extends StraightLineMoves {

    public QueenMoves(Board board, int location) {
        super(board, location);
    }

    @Override
    public void findCaptureAndNonCaptureMoves() {
        long queenAttacks = getBishopAttacks(location, occupiedBoard) | getRookAttacks(location, occupiedBoard);
        this.nonCaptureMoves = queenAttacks & ~occupiedBoard;
        this.captureMoves = queenAttacks & occupiedBoard & opponentBoard;
    }
}
