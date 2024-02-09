package com.stateofflux.chess.model;

import com.stateofflux.chess.model.player.Evaluator;

// Most Valuable Victim - Least Valuable Aggressor
// https://www.chessprogramming.org/MVV-LVA
public class MvvLvaMoveComparator implements java.util.Comparator<Move> {
    @Override
    public int compare(Move m1, Move m2) {
        // no captures
        if(!m1.isCapture() && !m2.isCapture())
            return 0;

        // TODO: Convert the formula into a lookup table.  Two benefits, faster and easier to tune.
        int m1Value = Evaluator.pieceToValue(m1.getCapturePiece()) * 100 + (Evaluator.KING_VALUE / Evaluator.pieceToValue(m1.getPiece()));
        int m2Value = Evaluator.pieceToValue(m2.getCapturePiece()) * 100 + (Evaluator.KING_VALUE / Evaluator.pieceToValue(m2.getPiece()));

        return m2Value - m1Value;
    }
}
