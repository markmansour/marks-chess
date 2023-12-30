package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class KnightMoves extends PieceMoves {
    static long[] KNIGHT_MOVES = new long[64];
    static {
        int rank;
        int file;
        long bb;

        for(int i = 0; i < 64; i++) {
            bb = 0L;
            rank = Board.rank(i) + 1;  // one offset
            file = Board.file(i);      // zero offset

            if (rank <= 7 && file >= 2) { bb |= 1L << (i +  6); } // 10 oclock
            if (rank <= 6 && file >= 1) { bb |= 1L << (i + 15); } // 11 oclock
            if (rank <= 6 && file <= 6) { bb |= 1L << (i + 17); } // 1 oclock
            if (rank <= 7 && file <= 5) { bb |= 1L << (i + 10); } // 2 oclock
            if (rank >= 2 && file <= 5) { bb |= 1L << (i + -6); } // 4 oclock
            if (rank >= 3 && file <= 6) { bb |= 1L << (i + -15); } // 5 oclock
            if (rank >= 3 && file >= 1) { bb |= 1L << (i + -17); } // 7 oclock
            if (rank >= 2 && file >= 2) { bb |= 1L << (i + -10); } // 8 oclock

            KNIGHT_MOVES[i] = bb;
        }
    }

    public KnightMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        // no-op
    }

    @Override
    protected void findCaptureAndNonCaptureMoves() {
        this.nonCaptureMoves |= KNIGHT_MOVES[this.location] & ~this.occupiedBoard;
        this.captureMoves |= KNIGHT_MOVES[this.location] & this.opponentBoard;
    }
}