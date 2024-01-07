package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class BishopMoves extends StraightLineMoves {
    public BishopMoves(Board board, int location) {
        super(board, location);
    }

    @Override
    public void findCaptureAndNonCaptureMoves() {
        long bishopAttacks = getBishopAttacks(location, occupiedBoard);
        this.nonCaptureMoves = bishopAttacks & ~occupiedBoard;
        this.captureMoves = bishopAttacks & occupiedBoard & opponentBoard;
    }
}