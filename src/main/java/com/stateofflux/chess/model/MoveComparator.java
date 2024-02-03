package com.stateofflux.chess.model;

public class MoveComparator implements java.util.Comparator<Move> {
    @Override
    public int compare(Move m1, Move m2) {
        return m2.getComparisonValue() - m1.getComparisonValue();
    }
}
