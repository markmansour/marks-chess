package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class KnightMoves implements PieceMovesInterface {
    public static final long[] KNIGHT_MOVES = new long[64];

    protected long nonCaptureMoves;
    protected long captureMoves;

    protected final Board board;
    protected final int location;
    protected final Piece piece;
    protected final boolean isWhite;

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
        this.board = board;
        this.location = location;
        this.isWhite = (((1L << location) & board.getWhite()) != 0);
        this.piece = isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;

        findCaptureAndNonCaptureMoves();
    }

    @Override
    public void findCaptureAndNonCaptureMoves() {
        long occupiedBoard = this.board.getOccupied();
        long opponentBoard = isWhite ? board.getBlack() : board.getWhite();

        this.nonCaptureMoves |= KNIGHT_MOVES[this.location] & ~occupiedBoard;
        this.captureMoves |= KNIGHT_MOVES[this.location] & opponentBoard;
    }

    public long getCaptureMoves() {
        return this.captureMoves;
    };

    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }
}