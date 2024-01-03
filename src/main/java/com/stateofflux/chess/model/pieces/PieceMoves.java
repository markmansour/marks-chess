package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import com.stateofflux.chess.model.PlayerColor;

public abstract class PieceMoves implements PieceMovesInterface {
    protected final Board board;
    protected int location;
    protected Piece piece;
    protected long nonCaptureMoves;
    protected long captureMoves;
    protected long occupiedBoard;
    protected long opponentBoard;
    protected long currentPlayerBoard;
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
     * @brief Returns the index of the MSB in the given bitboard or -1 if
     * the bitboard is empty.
     *
     * @param  l Bitboard to get MSB of
     * @return The index of the MSB in the given bitboard.

        inline int _bitscanReverse(U64 board) {
            if (board == ZERO) {
                return -1;
            }
            return 63 - __builtin_clzll(board);
        }

        __builtin_clz(x):
        This function returns number of leading 0-bits of x which starts from most significant bit position.
        e.g. __builtin_clz(16) = 27 because 16 is ' ... 10000'. Number of bits in a unsigned int is 32. so function returns 32 — 5 = 27.

    */
    static int bitscanReverse(long l) {
        if(l == 0L)
            return -1;

        return 63 - Long.numberOfLeadingZeros(l);
    }

    /**
     * @brief Returns the index of the LSB in the given bitboard or -1 if
     * the bitboard is empty.
     *
     * @param  l Bitboard to get LSB of
     * @return The index of the LSB in the given bitboard.
     *
     * note: original version uses __builtin_ffs
     *   => Returns one plus the index of the least significant 1-bit of x, or if x is zero, returns zero
     *   => e.g. __builtin_ffs(10) = 2 because 10 is '...1 0 1 0' in base 2 and first 1-bit from right is at index 1 (0-based) and function returns 1 + index.
     *   see: https://codeforces.com/blog/entry/15643?locale=en
     */
    public static int bitscanForward(long l) {
        if(l == 0L)
            return -1;

        return Long.numberOfTrailingZeros(l);
    }

    // Java implementation of __builtin_popcount - see https://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html
    // - count the number of set bits
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

    /**
     * @brief Sets the LSB of the given bitboard to 0 and returns its index.
     *
     * @param  mask Value to reset LSB of
     * @return Index of reset LSB
     */
    /*    private static int _popLsb(long mask) {
     *//*
     *   int lsbIndex = __builtin_ffsll(board) - 1;  // __builtin_ffsll - Returns one plus the index of the least significant 1-bit of x, or if x is zero, returns zero
     * board &= board - 1;  // LS-Bit-Reset - https://www.chessprogramming.org/Efficient_Generation_of_Sliding_Piece_Attacks#LS-Bit-Reset
     * return lsbIndex;
     *//*
        // long lsbIndex = Long.lowestOneBit(mask) - 1;
        return Long.numberOfLeadingZeros(mask);
    }*/

    // TODO: can this be set at instantiation?
    protected void setBoards() {
        if (isWhite) {
            this.currentPlayerBoard = board.getWhite();
            this.opponentBoard = board.getBlack();
        } else {
            this.currentPlayerBoard = board.getBlack();
            this.opponentBoard = board.getWhite();
        }
        ;
    }

    protected long getOpponentBoard() {
        return this.opponentBoard;
    }

    protected long getCurrentPlayerBoard() {
        return this.currentPlayerBoard;
    }

    public Board getBoard() {
        return this.board;
    }

    public Piece getPiece() {
        return this.piece;
    }

    @Override
    public long getCaptureMoves() {
        return this.captureMoves;
    }

    @Override
    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }

    // ---------------------------- static utilities ----------------------------
    public static int maxStepsToBoundary(int location, Direction direction) {
        return switch (direction) {
            case RIGHT, LEFT, DOWN, UP -> maxStepsToBoundaryHoriztonalOrVertical(location, direction);
            case UP_LEFT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case UP_RIGHT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.RIGHT));
            case DOWN_LEFT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case DOWN_RIGHT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.RIGHT));
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        };
    }

    public static int maxStepsToBoundaryHoriztonalOrVertical(int location, Direction direction) {
        return switch (direction) {
            case RIGHT -> 7 - Board.file(location);
            case LEFT -> Board.file(location);
            case DOWN -> Board.rank(location);
            case UP -> 7 - Board.rank(location);
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        };
        // check to see we're not going off the board.
    }
}
