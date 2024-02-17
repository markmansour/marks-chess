package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;

import static java.lang.Long.bitCount;

public class MaterialEvaluator implements Evaluator {

    protected boolean endGame = false;

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
            Evaluator.KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + Evaluator.QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + Evaluator.ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + Evaluator.BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + Evaluator.KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + Evaluator.PAWN_VALUE * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

        return (materialScore);
    }
}
