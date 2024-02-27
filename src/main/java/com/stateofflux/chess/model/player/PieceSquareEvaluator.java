package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;

import static java.lang.Long.bitCount;

/**
 * Returns the value of the board from White's perspective.
 */
public abstract class PieceSquareEvaluator implements Evaluator {
    // final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected int[][] pieceSquareTables;
    protected boolean endGame = false;

    public PieceSquareEvaluator() {
        pieceSquareTables = new int[12][];
    }

    /*
     * Assumes size of 64.
     */
    protected static int[] visualToArrayLayout(int[] visualLayout) {
        assert visualLayout.length == 64;

        int[] arrayLayout = new int[visualLayout.length];
        for (int i = 7; i >= 0; i--) {
            System.arraycopy(
                visualLayout, i * 8,
                arrayLayout, (7 - i) * 8,
                8);
        }

        return arrayLayout;
    }

    protected static int[] transposeWhiteToBlack(int[] a) {
        int[] result = new int[a.length];  // 64!

        for (int i = 0; i < 8; i++) {
            System.arraycopy(a, i * 8, result, 56 - (i * 8), 8);
        }

        return result;
    }

    /*
     * https://www.chessprogramming.org/Evaluation - symmetric evaluation function
     *
     * While Minimax usually associates the white side with the max-player and black with the min-player and always
     * evaluates from the white point of view, NegaMax requires a symmetric evaluation in relation to
     * the side to move.
     *
     */
    @Override
    public int evaluate(Game game, int depth) {
        Board b = game.getBoard();
        int bonus = 0;

        // sideMoved is the value of the move just completed.  The game counter has already moved
        // on, so we need to reverse the player color.  Therefore, if the game thinks it is white's turn
        // then it was black that just moved.
        int sideMoved = game.getActivePlayerColor().isWhite() ? 1 : -1;

        // from white's perspective.
        if (game.isCheckmated()) {
            // logger.info("**************** CHECKMATED: {}", evaluatingMoves);
            return (MATE_VALUE - depth) * -sideMoved;  //  - e.g. if it is whites turn then this is bad, so make it -ve
//        } else if (game.isDraw()) { // isDraw is very expensive for me to calculate (due to isStalemate) - so disable it.
//            return 0;
        }

        // from the perspective of the white player
        int materialScore =
            Evaluator.KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + Evaluator.QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + Evaluator.ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + Evaluator.BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + Evaluator.KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + Evaluator.PAWN_VALUE * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

        int mobilityWeight = 1;

        // using pseudo moves as:
        // a) it's faster than creating legal moves
        // b) the current game logic doesn't take the moving player into account when cleaning up moves.  e.g. if
        //    it is white's turn and I ask for black moves, it will remove any black moves that put white in check.
        MoveList<Move> whiteMoves = game.pseudoLegalMovesFor(PlayerColor.WHITE);
        MoveList<Move> blackMoves = game.pseudoLegalMovesFor(PlayerColor.BLACK);

        // from the perspective of the white player
        int mobilityScore = mobilityWeight *
            (whiteMoves.size() - blackMoves.size());

        if (game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            // LOGGER.info("**************** CHECK: {}", evaluatingMoves);
            bonus += 500 * -sideMoved;  // from white's perspective - e.g. if it is whites turn then this is bad, so make it -ve
        }

        // if attacking a space next to the king give a bonus

/*
        long kingAttacks = KingMoves.surroundingMoves(b.getKingLocation(getColor()));
        for(int dest: Board.bitboardToArray(kingAttacks)) {
            if(b.locationUnderAttack(getColor(), dest)) {
                bonus += (100 * sideToMove);
            }
        }
*/

        /*
         * In order for NegaMax to work, it is important to return the score relative to the side being evaluated.
         * materialScore, mobilityScore and bonus are all calculated from white's perspective, so need to be
         * multiplied by side.  BoardScore already takes into account the side moving.
         */
        return materialScore + mobilityScore + bonus + boardScore(game);
    }

    /*
     * return the board score from the perspective of white.
     */
    protected int boardScore(Game game) {
        int score = 0;
        Board b = game.getBoard();
        checkForEndGame(game);

        for (int i = 0; i < 64; i++) {
            Piece p = b.get(i);
            if (p.isEmpty()) continue;

            score += pieceSquareTables[p.getIndex()][i] * (p.isWhite() ? 1 : -1);
        }

        return score;
    }

     abstract void checkForEndGame(Game game);
}
