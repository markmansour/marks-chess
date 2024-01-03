package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public abstract class StraightLineMoves extends PieceMoves {

    private static final long[] ROOK_MASKS = new long[64];
    public static final long[][] ROOK_TABLE = new long[64][4096];
    private static final long[] ROOK_MAGIC = new long[]{
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
    private static final int[] ROOK_INDEX_BITS = new int[]{
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };
    private static final long[] BISHOP_MASKS = new long[64];
    public static final long[][] BISHOP_TABLE = new long[64][1024];
    private static final long[] BISHOP_MAGIC = new long[]{
        0x89a1121896040240L, 0x2004844802002010L, 0x2068080051921000L, 0x62880a0220200808L, 0x4042004000000L,
        0x100822020200011L, 0xc00444222012000aL, 0x28808801216001L, 0x400492088408100L, 0x201c401040c0084L,
        0x840800910a0010L, 0x82080240060L, 0x2000840504006000L, 0x30010c4108405004L, 0x1008005410080802L,
        0x8144042209100900L, 0x208081020014400L, 0x4800201208ca00L, 0xf18140408012008L, 0x1004002802102001L,
        0x841000820080811L, 0x40200200a42008L, 0x800054042000L, 0x88010400410c9000L, 0x520040470104290L,
        0x1004040051500081L, 0x2002081833080021L, 0x400c00c010142L, 0x941408200c002000L, 0x658810000806011L,
        0x188071040440a00L, 0x4800404002011c00L, 0x104442040404200L, 0x511080202091021L, 0x4022401120400L,
        0x80c0040400080120L, 0x8040010040820802L, 0x480810700020090L, 0x102008e00040242L, 0x809005202050100L,
        0x8002024220104080L, 0x431008804142000L, 0x19001802081400L, 0x200014208040080L, 0x3308082008200100L,
        0x41010500040c020L, 0x4012020c04210308L, 0x208220a202004080L, 0x111040120082000L, 0x6803040141280a00L,
        0x2101004202410000L, 0x8200000041108022L, 0x21082088000L, 0x2410204010040L, 0x40100400809000L,
        0x822088220820214L, 0x40808090012004L, 0x910224040218c9L, 0x402814422015008L, 0x90014004842410L,
        0x1000042304105L, 0x10008830412a00L, 0x2520081090008908L, 0x40102000a0a60140L
    };
    private static final int[] BISHOP_INDEX_BITS = new int[]{
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };

    static {
        initializeBishopMasks();
        initializeRookMasks();

        initializeBishopMagic();
        initializeRookMagic();
    }

    protected int max = 7; // max number of moves in any direction

    protected StraightLineMoves(Board board, int location) {
        super(board, location);
    }

    // good
    private static void initializeBishopMasks() {
        long edgeSquares = Board.FILE_A | Board.FILE_H | Board.RANK_1 | Board.RANK_8;
        for (int location = 0; location < 64; location++) {
            BISHOP_MASKS[location] =
                (Direction.getRay(Direction.UP_LEFT, location) |
                    Direction.getRay(Direction.DOWN_LEFT, location) |
                    Direction.getRay(Direction.DOWN_RIGHT, location) |
                    Direction.getRay(Direction.UP_RIGHT, location)) &
                    ~(edgeSquares);
        }
    }

    // good
    private static void initializeBishopMagic() {
        // For all squares
        for (int location = 0; location < 64; location++) {
            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < (1 << BISHOP_INDEX_BITS[location]); blockerIndex++) {
                long blockers = getBlockersFromIndex(blockerIndex, BISHOP_MASKS[location]);
                int key = (int) ((blockers * BISHOP_MAGIC[location]) >>> (64 - BISHOP_INDEX_BITS[location]));

                BISHOP_TABLE[location][key] = calculateBishopMoves(location, blockers);
            }
        }
    }

    // good
    private static long calculateBishopMoves(int square, long blockers) {
        long attacks = 0L;

        // North West
        attacks |= Direction.getRay(Direction.UP_LEFT, square);
        if ((Direction.getRay(Direction.UP_LEFT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.UP_LEFT, bitscanForward(Direction.getRay(Direction.UP_LEFT, square) & blockers)));
        }

        // North East
        attacks |= Direction.getRay(Direction.UP_RIGHT, square);
        if ((Direction.getRay(Direction.UP_RIGHT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.UP_RIGHT, bitscanForward(Direction.getRay(Direction.UP_RIGHT, square) & blockers)));
        }

        // South East
        attacks |= Direction.getRay(Direction.DOWN_RIGHT, square);
        if ((Direction.getRay(Direction.DOWN_RIGHT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.DOWN_RIGHT, bitscanReverse(Direction.getRay(Direction.DOWN_RIGHT, square) & blockers)));
        }

        // South West
        attacks |= Direction.getRay(Direction.DOWN_LEFT, square);
        if ((Direction.getRay(Direction.DOWN_LEFT, square) & blockers) != 0) {
            attacks &= ~(Direction.getRay(Direction.DOWN_LEFT, bitscanReverse(Direction.getRay(Direction.DOWN_LEFT, square) & blockers)));
        }

        return attacks;
    }

    // good
    public static long getBishopAttacks(int location, long blockers) {
        blockers &= BISHOP_MASKS[location];
        int key = (int) ((blockers * BISHOP_MAGIC[location]) >>> (64 - BISHOP_INDEX_BITS[location]));
        return BISHOP_TABLE[location][key];
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
    private static void initializeRookMagic() {
        for (int location = 0; location < 64; location++) {
            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < (1 << ROOK_INDEX_BITS[location]); blockerIndex++) {
                long blockers = getBlockersFromIndex(blockerIndex, ROOK_MASKS[location]);
                int key = (int) ((blockers * ROOK_MAGIC[location]) >>> (64 - ROOK_INDEX_BITS[location]));
                ROOK_TABLE[location][key] = StraightLineMoves.calculateRookMoves(location, blockers);
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

    public static long getRookAttacks(int square, long blockers) {
        blockers &= ROOK_MASKS[square];
        int key = (int) ((blockers * ROOK_MAGIC[square]) >>> (64 - ROOK_INDEX_BITS[square]));
        return ROOK_TABLE[square][key];
    }
}