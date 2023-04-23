package com.stateofflux.chess.model;

public class KingMoves extends BoardMoves {
    public static class Builder extends BoardMoves.Builder<Builder> {

        protected Builder(Board board, int location) {
            super(board, location);
            this.max = 1;
        }
/*
        @Override
        protected Builder moving(Direction[] directions) {
            // do nothing. Should this method exist?
            return self();
        }
 */
        @Override
        protected BoardMoves getInstance() {
            return new KingMoves(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private KingMoves(Builder builder) {
        super(builder);
    }

}
