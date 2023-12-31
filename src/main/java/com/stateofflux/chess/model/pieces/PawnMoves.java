package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class PawnMoves extends StraightLineMoves {
    public static final String NO_EN_PASSANT = "-";
    public static final int NO_EN_PASSANT_VALUE = -1;

    private final int enPassantTarget;

    public PawnMoves(Board board, int location, int enPassant) {
        super(board, location);
        this.enPassantTarget = enPassant;
        findEnPassantCaptures();
    }

    protected void setupPaths() {
    }

    @Override
    public boolean isCheckingForCaptures() {
        return false;
    }

    private static final long[][] PAWN_ATTACKS = new long[2][64];

    static {
        initializePawnAttacks();
    }

    private static void initializePawnAttacks() {
        for(int location = 0; location < 64; location++) {
            long start = 1L << location;

            long white = ((start << 9L) & ~Board.FILE_A) | ((start << 7) & ~Board.FILE_H);
            long black = ((start >> 9L) & ~Board.FILE_H) | ((start >> 7) & ~Board.FILE_A);

            PAWN_ATTACKS[0][location] = white;
            PAWN_ATTACKS[1][location] = black;
        }

    }

    @Override
    public void findCaptureAndNonCaptureMoves() {
        long pawns;
        boolean isWhite = piece.isWhite();
        long oneStep, twoStep;

        // one step forward
        if(isWhite) {
            pawns = board.getWhitePawns() & (1L << this.location);
            oneStep = (pawns << 8L) & ~occupiedBoard;
        } else {
            pawns = board.getBlackPawns() & (1L << this.location);
            oneStep = (pawns >> 8L) & ~occupiedBoard;
        }

        // two steps forward
        if(isWhite) {
            twoStep = ((oneStep & Board.RANK_3) << 8L) & ~occupiedBoard;
        } else {
            twoStep = ((oneStep & Board.RANK_6) >> 8L) & ~occupiedBoard;
        }

        this.nonCaptureMoves = oneStep | twoStep;

        // capture
        if(isWhite) {
            this.captureMoves |= PAWN_ATTACKS[0][this.location] & opponentBoard;
        } else {
            this.captureMoves |= PAWN_ATTACKS[1][this.location] & opponentBoard;
        }
    }

    private void findEnPassantCaptures() {
        // if there is n en passant target, then exit
        if (this.enPassantTarget == -1) {
            return;
        }

        boolean isWhite = piece.isWhite();
        int file = Board.file(this.enPassantTarget);

        if(isWhite) {
            if (file > 0 && this.enPassantTarget == (this.location + 9))  // white left
                this.captureMoves |= (1L << this.enPassantTarget);
            else if (file < 7 && this.enPassantTarget == (this.location + 7))  // white right
                this.captureMoves |= (1L << this.enPassantTarget);
        } else {
            if (file > 0 && this.enPassantTarget == (this.location - 9))  // black left
                this.captureMoves |= (1L << this.enPassantTarget);
            else if (file < 7 && this.enPassantTarget == (this.location - 7))  // black right
                this.captureMoves |= (1L << this.enPassantTarget);
        }
    }
}
