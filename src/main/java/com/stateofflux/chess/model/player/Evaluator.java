package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.pieces.Piece;

public interface Evaluator {
    public static final int PAWN_VALUE = 100;
    public static final int ROOK_VALUE = 500;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20_000;
    public static final int MATE_VALUE = 100_000;
    public static final int MIN_VALUE = -1_000_000;
    public static final int MAX_VALUE = 1_000_000;

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
