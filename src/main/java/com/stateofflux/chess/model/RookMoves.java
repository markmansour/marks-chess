package com.stateofflux.chess.model;

public class RookMoves {
    public static BoardMoves from(Board board, int location) {
        return new BoardMoves.Builder(board, location)
                .moveAndCaptureDirections(new Direction[] {
                        Direction.UP,
                        Direction.RIGHT,
                        Direction.DOWN,
                        Direction.LEFT
                })
                .max(7)
                .build();
    }

    // hide the public constructor
    private RookMoves() {
        super();
    }
}
