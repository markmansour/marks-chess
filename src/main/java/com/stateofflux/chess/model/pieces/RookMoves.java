package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RookMoves extends StraightLineMoves {
    private static final Logger LOGGER = LoggerFactory.getLogger(RookMoves.class);

    public static Direction[] ROOK_DIRECTIONS = new Direction[]{
        Direction.UP,
        Direction.RIGHT,
        Direction.DOWN,
        Direction.LEFT
    };

    static final long[] ROOK_MASKS = new long[64];
    static final long[][] ROOK_TABLE = new long[64][4096];

    static final long[] ROOK_MAGIC = new long[] {
        0xa8002c000108020L, 0x6c00049b0002001L, 0x100200010090040L, 0x2480041000800801L, 0x280028004000800L,
        0x900410008040022L, 0x280020001001080L, 0x2880002041000080L, 0xa000800080400034L, 0x4808020004000L,
        0x2290802004801000L, 0x411000d00100020L, 0x402800800040080L, 0xb000401004208L, 0x2409000100040200L,
        0x1002100004082L, 0x22878001e24000L, 0x1090810021004010L, 0x801030040200012L, 0x500808008001000L,
        0xa08018014000880L, 0x8000808004000200L, 0x201008080010200L, 0x801020000441091L, 0x800080204005L,
        0x1040200040100048L, 0x120200402082L, 0xd14880480100080L, 0x12040280080080L, 0x100040080020080L,
        0x9020010080800200L, 0x813241200148449L, 0x491604001800080L, 0x100401000402001L, 0x4820010021001040L,
        0x400402202000812L, 0x209009005000802L, 0x810800601800400L, 0x4301083214000150L, 0x204026458e001401L,
        0x40204000808000L, 0x8001008040010020L, 0x8410820820420010L, 0x1003001000090020L, 0x804040008008080L,
        0x12000810020004L, 0x1000100200040208L, 0x430000a044020001L, 0x280009023410300L, 0xe0100040002240L,
        0x200100401700L, 0x2244100408008080L, 0x8000400801980L, 0x2000810040200L, 0x8010100228810400L,
        0x2000009044210200L, 0x4080008040102101L, 0x40002080411d01L, 0x2005524060000901L, 0x502001008400422L,
        0x489a000810200402L, 0x1004400080a13L, 0x4000011008020084L, 0x26002114058042L
    };

    static final int[] ROOK_INDEX_BITS = new int[] {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };

    static {
        initializeRookMasks();
        initRookMagic();
    }

    private static void initializeRookMasks() {
        for (int square = 0; square < 64; square++) {
            ROOK_MASKS[square] =
                Direction.getRay(Direction.UP, square) & ~Board.RANK_8 |
                    Direction.getRay(Direction.DOWN, square) & ~Board.RANK_1 |
                    Direction.getRay(Direction.RIGHT, square) & ~Board.FILE_H |
                    Direction.getRay(Direction.LEFT, square) & ~Board.FILE_A;
        }
    }

    // See article https://essays.jwatzman.org/essays/chess-move-generation-with-magic-bitboards.html
    /*
    void Attacks::detail::_initRookMagicTable() {
        // For all squares
        for (int square = 0; square < 64; square++) {
            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < (1 << _rookIndexBits[square]); blockerIndex++) {
                U64 blockers = _getBlockersFromIndex(blockerIndex, _rookMasks[square]);
                _rookTable[square][(blockers * _rookMagics[square]) >> (64 - _rookIndexBits[square])] =
                    _getRookAttacksSlow(square, blockers);
            }
        }
    }
    */
    private static void initRookMagic() {
        for (int square = 0; square < 64; square++) {
            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < (1 << ROOK_INDEX_BITS[square]); blockerIndex++) {
                long blockers = getBlockersFromIndex(blockerIndex, ROOK_MASKS[square]);
                int key = (int) ((blockers * ROOK_MAGIC[square]) >>> (64 - ROOK_INDEX_BITS[square]));
                ROOK_TABLE[square][key] = calculateRookMoves(square, blockers);
            }
        }
    }

    private static long calculateRookMoves(int square, long blockers) {
        long attacks = 0L;

        // North
        attacks |= Direction.getRay(Direction.UP, square);
        if ((Direction.getRay(Direction.UP, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.UP, bitscanForward(Direction.getRay(Direction.UP, square) & blockers)));
        }

        // South
        attacks |= Direction.getRay(Direction.DOWN, square);
        if ((Direction.getRay(Direction.DOWN, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.DOWN, bitscanReverse(Direction.getRay(Direction.DOWN, square) & blockers)));
        }

        // East
        attacks |= Direction.getRay(Direction.RIGHT, square);
        if ((Direction.getRay(Direction.RIGHT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.RIGHT, bitscanForward(Direction.getRay(Direction.RIGHT, square) & blockers)));
        }

        // West
        attacks |= Direction.getRay(Direction.LEFT, square);
        if ((Direction.getRay(Direction.LEFT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.LEFT, bitscanReverse(Direction.getRay(Direction.LEFT, square) & blockers)));
        }

        return attacks;
    }

    public static int ROOK_DIRECTIONS_MAX = 7;

    public RookMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = ROOK_DIRECTIONS;
        this.max = ROOK_DIRECTIONS_MAX;
    }

    private static long getRookAttacks(int square, long blockers) {
        blockers &= ROOK_MASKS[square];
        int key = (int) ((blockers * ROOK_MAGIC[square]) >>> (64 - ROOK_INDEX_BITS[square]));
        return ROOK_TABLE[square][key];
    }

    @Override
    protected void findCaptureAndNonCaptureMoves() {
        long rookAttacks = getRookAttacks(location, occupiedBoard);
        this.nonCaptureMoves = rookAttacks & ~occupiedBoard;
        this.captureMoves = rookAttacks & occupiedBoard & opponentBoard;
    }
}