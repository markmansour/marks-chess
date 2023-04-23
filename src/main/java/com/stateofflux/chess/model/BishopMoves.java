package com.stateofflux.chess.model;

public class BishopMoves extends BoardMoves {
    public static class Builder extends BoardMoves.Builder<Builder> {

        protected Builder(Board board, int location) {
            super(board, location);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected Builder moving(Direction[] directions) {
            // do nothing. Should this method exist?
            return self();
        }

        @Override
        protected void allowedDirections() {
            this.directions = new Direction[] {
                    Direction.UP_LEFT,
                    Direction.UP_RIGHT,
                    Direction.DOWN_LEFT,
                    Direction.DOWN_RIGHT
            };
        }

        @Override
        protected BoardMoves getInstance() {
            return new BishopMoves(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private BishopMoves(Builder builder) {
        super(builder);
        // TODO Auto-generated constructor stub
    }
}
