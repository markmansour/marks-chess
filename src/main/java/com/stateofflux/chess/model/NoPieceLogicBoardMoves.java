package com.stateofflux.chess.model;

public class NoPieceLogicBoardMoves extends BoardMoves {
    public static class Builder extends BoardMoves.Builder<Builder> {

        protected Builder(Board board, int location) {
            super(board, location);
            // TODO Auto-generated constructor stub
        }

        @Override
        public BoardMoves build() {
            findMovesInStraightLines();
            return new NoPieceLogicBoardMoves(this);
        }

        @Override
        protected Builder self() { return this; }
    }

    private NoPieceLogicBoardMoves(Builder builder) {
        super(builder);
        // TODO Auto-generated constructor stub
    }
}
