package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public abstract class PieceMoves implements PieceMovesInterface {
    protected final Board board;
    protected final int location;
    protected final Piece piece;
    protected long nonCaptureMoves;
    protected long captureMoves;
    protected final long occupiedBoard;
    protected long opponentBoard;
    protected final boolean isWhite;

    protected PieceMoves(Board board, int location) {
        this.board = board;
        this.location = location;
        this.isWhite = (((1L << location) & board.getWhite()) != 0);

        // are these always needed? Can we late bind them?
        this.occupiedBoard = this.board.getOccupied();  // the union of current and occupied boards
        this.piece = board.get(location);
        setBoards();
        findCaptureAndNonCaptureMoves();
    }

    /**
     * Returns the index of the MSB in the given bitboard or -1 if
     * the bitboard is empty.
     *
     * @param  l Bitboard to get MSB of
     * @return The index of the MSB in the given bitboard.
    */
    static int bitscanReverse(long l) {
        if(l == 0L)
            return -1;

        return 63 - Long.numberOfLeadingZeros(l);
    }

    /**
     * Returns the index of the LSB in the given bitboard or -1 if
     * the bitboard is empty.
     *
     * @param  l Bitboard to get LSB of
     * @return The index of the LSB in the given bitboard.
     *
     * note: original version uses __builtin_ffs
     *   => Returns one plus the index of the least significant 1-bit of x, or if x is zero, returns zero
     *   => e.g. __builtin_ffs(10) = 2 because 10 is '...1 0 1 0' in base 2 and first 1-bit from right is at index 1 (0-based) and function returns 1 + index.
     *   see: <a href="https://codeforces.com/blog/entry/15643?locale=en">C++ code</a>.
     */
    public static int bitscanForward(long l) {
        if(l == 0L)
            return -1;

        return Long.numberOfTrailingZeros(l);
    }

    /**
     * Java implementation of __builtin_popcount - see <a href="https://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html">gcc builtins</a>
     * count the number of set bits
     */
    public static int popCount(long board) {
        return Long.bitCount(board);  // Returns the number of 1-bits in x
    }

    static long getBlockersFromIndex(int index, long mask) {
        long blockers = 0L;
        int bits = popCount(mask);  // verified - I think this is right.

        for (int i = 0; i < bits; i++) {
            // int bitPos = _popLsb(mask); // replaced with the following two lines.
            // Returns one plus the index of the least significant 1-bit of x, or if x is zero, returns zero
            int bitPos = Long.numberOfTrailingZeros(mask);  //
            mask &= (mask - 1L);

            if ((index & (1L << i)) != 0) {
                blockers |= (1L << bitPos);
            }
        }
        return blockers;
    }

    protected void setBoards() {
        if (isWhite) {
            this.opponentBoard = board.getBlack();
        } else {
            this.opponentBoard = board.getWhite();
        }
    }

    @Override
    public long getCaptureMoves() {
        return this.captureMoves;
    }

    @Override
    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }
}
