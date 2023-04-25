package com.stateofflux.chess.model;

public class KnightMoves extends StraightLineMoves {
        protected KnightMoves(Board board, int location) {
                super(board, location);
                // this is wrong
                setupPaths();
        }

        protected void setupPaths() {
                this.directions = new Direction[] {
                                Direction.UP_LEFT,
                                Direction.UP_RIGHT,
                                Direction.DOWN_LEFT,
                                Direction.DOWN_RIGHT
                };
        }

        @Override
        public void findCaptureAndNonCaptureMovesInStraightLines() {
        }
}