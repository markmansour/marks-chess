package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class BishopMoves extends StraightLineMoves {

    public static final int BISHOP_DIRECTIONS_MAX = 7;
    private static final long[] BISHOP_MASKS = new long[64];
    private static final long[][] BISHOP_TABLE = new long[64][1024];


    private static final long[] BISHOP_MAGIC = new long[] {
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

    private static final int[] BISHOP_INDEX_BITS = new int[] {
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
        initializeBishopMagic();
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

    public BishopMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.directions = new Direction[] {
                Direction.UP_LEFT,
                Direction.UP_RIGHT,
                Direction.DOWN_LEFT,
                Direction.DOWN_RIGHT
        };
        this.max = BISHOP_DIRECTIONS_MAX;
    }

    // good
    private static long getBishopAttacks(int location, long blockers) {
        blockers &= BISHOP_MASKS[location];
        int key = (int) ((blockers * BISHOP_MAGIC[location]) >>> (64 - BISHOP_INDEX_BITS[location]));
        return BISHOP_TABLE[location][key];
    }

    @Override
    protected void findCaptureAndNonCaptureMoves() {
        long bishopAttacks = getBishopAttacks(location, occupiedBoard);
        this.nonCaptureMoves = bishopAttacks & ~occupiedBoard;
        this.captureMoves = bishopAttacks & occupiedBoard & opponentBoard;
    }
}