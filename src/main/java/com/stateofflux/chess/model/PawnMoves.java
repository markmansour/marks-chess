package com.stateofflux.chess.model;

public class PawnMoves {
    public static BoardMoves from(Board board, int location) {
        Piece piece = board.getPieceAtLocation(location);

        Direction d;
        int max = 2;

        if (piece.isBlack()) {
            d = Direction.DOWN;
            if (location < 48) { // no longer on rank 7
                max = 1;
            }
        } else if (piece.isWhite()) {
            d = Direction.UP;
            if (location >= 16) { // no longer on rank 2
                max = 1;
            }
        } else
            throw new IllegalArgumentException("Piece must be black or white");

        return new BoardMoves.Builder(board, location)
                .moveAndCaptureDirections(new Direction[] { d })
                .max(max)
                .build();
    }

    // hide the public constructor
    private PawnMoves() {
        super();
    }

}
