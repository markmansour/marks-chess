package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;

import static java.lang.Long.bitCount;

public class MaterialEvaluator implements Evaluator {
    private static final int PAWN_VALUE = 100;
    private static final int ROOK_VALUE = 500;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20_000;

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

    public MaterialEvaluator() {
    }

    @Override
    public int evaluate(Game game, int depth) {
        Board b = game.getBoard();

        // sideMoved is the value of the move just completed.  The game counter has already moved
        // on, so we need to reverse the player color.  Therefore, if the game thinks it is white's turn
        // then it was black that just moved.
        int sideMoved = game.getActivePlayerColor().isBlack() ? 1 : -1;

        // from white's perspective.
        if(game.isCheckmated()) {
            // LOGGER.info("**************** CHECKMATED: {}", evaluatingMoves);
            return (MATE_VALUE - depth) * sideMoved;
        }

        // from the perspective of the white player
        int materialScore =
            KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + PAWN_VALUE * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

        return (materialScore);
    }

    @Override
    public String toString() {
        return "MaterialEvaluator";
    }
}
