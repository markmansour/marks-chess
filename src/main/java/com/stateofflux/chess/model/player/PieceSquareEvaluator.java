package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;

import static java.lang.Long.bitCount;

/**
 * Returns the value of the board from White's perspective.
 */
public abstract class PieceSquareEvaluator implements Evaluator {
    protected static int PAWN_VALUE = 100;
    protected static final int ROOK_VALUE = 500;
    protected static final int KNIGHT_VALUE = 320;
    protected static final int BISHOP_VALUE = 330;
    protected static final int QUEEN_VALUE = 900;
    protected static final int KING_VALUE = 20_000;

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
     * the side to move.  Therefore, is white is winning, return a large number.  If black is winning, returning
     * a large number.
     */
    @Override
    public int evaluate(Game game, int depthTraversed) {
        Board b = game.getBoard();
        int bonus = 0;

        if (game.isCheckmated()) {
            return (MATE_VALUE - depthTraversed) * -1;
        }

        // from the perspective of the white player
        int materialScore =
            KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + PAWN_VALUE * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

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

/*
        if (game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            // LOGGER.info("**************** CHECK: {}", evaluatingMoves);
            bonus += 500 * -currentPlayerAsInt;  // from white's perspective - e.g. if it is whites turn then this is bad, so make it -ve
        }
*/

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
        int currentPlayerAsInt = game.getActivePlayerColor().isWhite() ? 1 : -1;

        return (materialScore + mobilityScore + bonus + boardScore(game)) * currentPlayerAsInt;
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

    @Override
    public String toString() {
        return "PieceSquareEvaluator";
    }
}
