package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.pieces.Piece;

public interface Evaluator {
    int PAWN_VALUE = 100;
    int ROOK_VALUE = 500;
    int KNIGHT_VALUE = 320;
    int BISHOP_VALUE = 330;
    int QUEEN_VALUE = 900;
    int KING_VALUE = 20_000;

    static int pieceToValue(Piece piece) {
        if(piece == null)
            return 0;

        return switch(piece.getAlgebraicChar()) {
            case Piece.PAWN_ALGEBRAIC -> PAWN_VALUE;
            case Piece.ROOK_ALGEBRAIC -> ROOK_VALUE;
            case Piece.KNIGHT_ALGEBRAIC -> KNIGHT_VALUE;
            case Piece.BISHOP_ALGEBRAIC -> BISHOP_VALUE;
            case Piece.QUEEN_ALGEBRAIC -> QUEEN_VALUE;
            case Piece.KING_ALGEBRAIC -> KING_VALUE;
            default -> 0;  // EMPTY
        };
    }

    int evaluate(Game game, int depth);

    int getNodesEvaluated();
}
