package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class KnightMoves extends PieceMoves {
    int[] paths;

    public KnightMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        // no-op
    }

    @Override
    void findCaptureAndNonCaptureMoves() {
        int rank = this.location / 8 + 1;  // one offset
        int file = this.location % 8;      // zero offset

        if (rank <= 7 && file >= 2) { checkKnightMove(6); } // 10 oclock
        if (rank <= 6 && file >= 1) { checkKnightMove(15); } // 11 oclock

        if (rank <= 6 && file <= 6) { checkKnightMove(17); } // 1 oclock
        if (rank <= 7 && file <= 5) { checkKnightMove(10); } // 2 oclock

        if (rank >= 2 && file <= 5) { checkKnightMove(-6); } // 4 oclock
        if (rank >= 3 && file <= 6) { checkKnightMove(-15); } // 5 oclock

        if (rank >= 3 && file >= 1) { checkKnightMove(-17); } // 7 oclock
        if (rank >= 2 && file >= 2) { checkKnightMove(-10); } // 8 oclock
    }

    private void checkKnightMove(int offset) {
        int nextPosition;
        long nextPositionBit;

        nextPosition = this.location + offset;
        nextPositionBit = 1L << nextPosition;

        if ((this.occupiedBoard & nextPositionBit) == 0)
            this.nonCaptureMoves |= nextPositionBit;

        if ((this.opponentBoard & nextPositionBit) != 0) {
            this.captureMoves |= nextPositionBit;
        }
    }
}