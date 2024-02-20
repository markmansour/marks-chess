package com.stateofflux.chess.model;

import java.io.Serializable;

public class CaptureOnlyMoveComparator implements java.util.Comparator<Move>, Serializable {
    @Override
    public int compare(Move m1, Move m2) {
        return m2.getComparisonValue() - m1.getComparisonValue();
    }
}
