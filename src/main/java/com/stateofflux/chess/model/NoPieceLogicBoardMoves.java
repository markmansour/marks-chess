package com.stateofflux.chess.model;

public class NoPieceLogicBoardMoves {
    public static BoardMoves from(Board board, int location) {
        return new BoardMoves.Builder(board, location)
                .moveAndCaptureDirections(new Direction[] {
                        Direction.UP_LEFT,
                        Direction.UP,
                        Direction.UP_RIGHT,
                        Direction.RIGHT,
                        Direction.DOWN_RIGHT,
                        Direction.DOWN,
                        Direction.DOWN_LEFT,
                        Direction.LEFT
                })
                .max(7)
                .build();
    }

    // hide the public constructor
    private NoPieceLogicBoardMoves() {
        super();
    }
}
