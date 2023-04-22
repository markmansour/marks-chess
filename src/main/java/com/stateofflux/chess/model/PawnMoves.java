package com.stateofflux.chess.model;

public class PawnMoves extends BoardMoves {
    public static class Builder extends BoardMoves.Builder<Builder> {

        protected Builder(Board board, int location) {
            super(board, location);
            // TODO Auto-generated constructor stub
        }

        @Override
        public PawnMoves build() {
            return new PawnMoves(this);
        }

        @Override
        protected Builder self() { return this; }
    }

    private PawnMoves(Builder builder) {
        super(builder);
        // TODO Auto-generated constructor stub
    }
}
