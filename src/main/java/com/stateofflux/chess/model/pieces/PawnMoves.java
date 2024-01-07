package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public class PawnMoves implements PieceMovesInterface {
    public static final long[][] PAWN_ATTACKS = new long[2][64];

    public static final String NO_EN_PASSANT = "-";
    public static final int NO_EN_PASSANT_VALUE = -1;

    protected long nonCaptureMoves;
    protected long captureMoves;

    private final int enPassantTarget;
    protected final Board board;
    protected final int location;
    protected final Piece piece;
    protected final boolean isWhite;


    static {
        initializePawnAttacks();
    }

    public PawnMoves(Board board, int location, int enPassant) {
        this.board = board;
        this.location = location;
        this.enPassantTarget = enPassant;
        this.isWhite = (((1L << location) & board.getWhite()) != 0);
        this.piece = isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;

        findCaptureAndNonCaptureMoves();
    }

    private static void initializePawnAttacks() {
        for(int location = 0; location < 64; location++) {
            long start = 1L << location;

            long white = ((start << 9L) & ~Board.FILE_A) | ((start << 7L) & ~Board.FILE_H);
            long black = ((start >>> 9L) & ~Board.FILE_H) | ((start >>> 7L) & ~Board.FILE_A);

            PAWN_ATTACKS[0][location] = white;
            PAWN_ATTACKS[1][location] = black;
        }

    }

    public void findCaptureAndNonCaptureMoves() {
        if(isWhite)
            findCaptureAndNonCaptureWhiteMoves();
        else
            findCaptureAndNonCaptureBlackMoves();
    }

    public void findCaptureAndNonCaptureWhiteMoves() {
        long oneStep, twoStep;
        long occupiedBoard = board.getOccupied();
        long pawns;
        long opponentBoard = board.getBlack();

        // one step forward
        pawns = board.getWhitePawns() & (1L << location);
        oneStep = (pawns << 8L) & ~occupiedBoard;

        // two steps forward
        twoStep = ((oneStep & Board.RANK_3) << 8L) & ~occupiedBoard;

        nonCaptureMoves = oneStep | twoStep;

        // capture
        captureMoves |= PAWN_ATTACKS[0][location] & opponentBoard;

        findEnPassantCaptures();
    }

    public void findCaptureAndNonCaptureBlackMoves() {
        long oneStep, twoStep;
        long occupiedBoard = board.getOccupied();
        long pawns;
        long opponentBoard = board.getWhite();

        // one step forward
        pawns = board.getBlackPawns() & (1L << location);
        oneStep = (pawns >> 8L) & ~occupiedBoard;

        // two steps forward
        twoStep = ((oneStep & Board.RANK_6) >> 8L) & ~occupiedBoard;

        nonCaptureMoves = oneStep | twoStep;

        // capture
        captureMoves |= PAWN_ATTACKS[1][location] & opponentBoard;

        findEnPassantCaptures();
    }


    private void findEnPassantCaptures() {
        // if there is n en passant target, then exit
        if (enPassantTarget == -1) {
            return;
        }

        if(isWhite)
            findWhiteEnPassantCaptures();
        else
            findBlackEnPassantCaptures();
    }

    private void findWhiteEnPassantCaptures() {
        int file = Board.file(enPassantTarget);

        if (file > 0 && enPassantTarget == (location + 9))  // white left
            captureMoves |= (1L << enPassantTarget);
        else if (file < 7 && enPassantTarget == (location + 7))  // white right
            captureMoves |= (1L << enPassantTarget);
    }

    private void findBlackEnPassantCaptures() {
        int file = Board.file(enPassantTarget);

        if (file > 0 && enPassantTarget == (location - 9))  // black left
            captureMoves |= (1L << enPassantTarget);
        else if (file < 7 && enPassantTarget == (location - 7))  // black right
            captureMoves |= (1L << enPassantTarget);
    }

    public long getCaptureMoves() {
        return captureMoves;
    };

    public long getNonCaptureMoves() {
        return nonCaptureMoves;
    }
}
