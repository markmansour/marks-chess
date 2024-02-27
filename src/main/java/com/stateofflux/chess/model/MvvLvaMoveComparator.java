package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.Piece;
import com.stateofflux.chess.model.player.Evaluator;

import java.io.Serializable;

// Most Valuable Victim - Least Valuable Aggressor
// https://www.chessprogramming.org/MVV-LVA
public class MvvLvaMoveComparator implements java.util.Comparator<Move>, Serializable {
    static int pieceToValue(Piece piece) {
        if(piece == null)
            return 0;

        return switch(piece.getAlgebraicChar()) {
            case Piece.PAWN_ALGEBRAIC -> 100;
            case Piece.ROOK_ALGEBRAIC -> 500;
            case Piece.KNIGHT_ALGEBRAIC -> 320;
            case Piece.BISHOP_ALGEBRAIC -> 330;
            case Piece.QUEEN_ALGEBRAIC -> 900;
            case Piece.KING_ALGEBRAIC -> 20_000;
            default -> 0;  // EMPTY
        };
    }

    @Override
    public int compare(Move m1, Move m2) {
        // no captures
        if(!m1.isCapture() && !m2.isCapture())
            return 0;

        // TODO: Convert the formula into a lookup table.  Two benefits, faster and easier to tune.
        int m1Value = pieceToValue(m1.getCapturePiece()) * 100 + (20_000 / pieceToValue(m1.getPiece()));
        int m2Value = pieceToValue(m2.getCapturePiece()) * 100 + (20_000 / pieceToValue(m2.getPiece()));

        return m2Value - m1Value;
    }
}
