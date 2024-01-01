package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class QueenMoves extends StraightLineMoves {

    public static final int QUEEN_DIRECTIONS_MAX = 7;

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
