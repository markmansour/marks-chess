package com.stateofflux.chess.model;

public class BishopMoves {
    public static BoardMoves from(Board board, int location) {
        return new BoardMoves.Builder(board, location)
                .moveAndCaptureDirections(new Direction[] {
                        Direction.UP_LEFT,
                        Direction.UP_RIGHT,
                        Direction.DOWN_LEFT,
                        Direction.DOWN_RIGHT
                })
                .max(7)
                .build();
    }

    // hide the public constructor
    private BishopMoves() {
        super();
    }
}
