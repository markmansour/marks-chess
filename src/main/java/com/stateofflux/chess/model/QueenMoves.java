package com.stateofflux.chess.model;

public class QueenMoves extends BoardMoves {
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
        protected BoardMoves getInstance() {
            return new QueenMoves(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private QueenMoves(Builder builder) {
        super(builder);
    }
}
