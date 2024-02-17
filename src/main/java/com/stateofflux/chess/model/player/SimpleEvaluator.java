package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static java.lang.Long.bitCount;

public class SimpleEvaluator implements Evaluator {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /*
     Tables from: https://www.chessprogramming.org/Simplified_Evaluation_Function
     But!  The tables listed are in the wrong order (really!?) as they are in visual order rather than array order.
     e.g.  Bottom left represents index 0, but if copied straight into an array would be index 54.  Therefore, reverse
     the line order so the bottom line (bottom 8 values) becomes the top line (the first 8 values).
    */
    protected static final int[] PAWN_TABLE = {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    };

    protected static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50,
    };

    protected static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20,
    };

    protected static final int[] ROOK_TABLE = {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    };

    protected static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    protected static final int[] KING_MIDGAME_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    };

    /*
     * Additionally we should define where the ending begins. For me it might be either if:
     * - Both sides have no queens or
     * - Every side which has a queen has additionally no other pieces or one minorpiece maximum.
     */
    protected static final int[] KING_ENDGAME_TABLE = {
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };

    /*
     * Assumes size of 64.
     */
    protected static int[] visualToArrayLayout(int[] visualLayout) {
        assert visualLayout.length == 64;

        int[] arrayLayout = new int[visualLayout.length];
        for(int i = 7; i >= 0; i--) {
            System.arraycopy(
                visualLayout, i * 8,
                arrayLayout, (7-i)*8,
                8);
        }

        return arrayLayout;
    }

    protected static int[] transposeWhiteToBlack(int[] a) {
        int [] result = new int[a.length];  // 64!

        for(int i = 0; i < 8; i++) {
            System.arraycopy(a, i * 8, result, 56 - (i * 8), 8);
        }

        return result;
    }


    protected int[][] PieceSquareTables;

    protected void initializePSTs() {
        PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(KING_MIDGAME_TABLE));
        PieceSquareTables[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        PieceSquareTables[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(transposeWhiteToBlack(QUEEN_TABLE));
        PieceSquareTables[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        PieceSquareTables[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(BISHOP_TABLE));
        PieceSquareTables[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        PieceSquareTables[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(KNIGHT_TABLE));
        PieceSquareTables[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        PieceSquareTables[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(ROOK_TABLE));
        PieceSquareTables[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        PieceSquareTables[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(PAWN_TABLE));
    }

    protected boolean endGame = false;

    public SimpleEvaluator() {
        PieceSquareTables = new int[12][];
        initializePSTs();
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
        int sideMoved = game.getActivePlayerColor().isBlack() ? 1 : -1;

        // from white's perspective.
        if(game.isCheckmated()) {
            // logger.info("**************** CHECKMATED: {}", evaluatingMoves);
            return (MATE_VALUE - depth) * sideMoved;
        } else if(game.isDraw()) {
            return 0;
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

        if(game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            // LOGGER.info("**************** CHECK: {}", evaluatingMoves);
            bonus += 500 * sideMoved;  // from white's perspective
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
        return (materialScore + mobilityScore + bonus) + (boardScore(game) * sideMoved);
    }

    protected int boardScore(Game game) {
        int score = 0;
        Board b = game.getBoard();
        checkForEndGame(game);

        for(int i = 0; i < 64; i++) {
            Piece p = b.get(i);
            if(p.isEmpty()) continue;

            score += PieceSquareTables[p.getIndex()][i] * (p.isWhite() ? 1 : -1);
        }

        return score;
    }

    private void checkForEndGame(Game game) {
        if (endGame) return;

        if(bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
            PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_ENDGAME_TABLE);
            PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(KING_ENDGAME_TABLE));
        }
    }
}
