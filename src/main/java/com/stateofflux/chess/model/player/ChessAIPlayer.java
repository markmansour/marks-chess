package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Implementation of https://github.com/zeyu2001/chess-ai/blob/main/js/main.js
 */
public class ChessAIPlayer extends BasicNegaMaxPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChessAIPlayer.class);

    protected static Map<Character, Integer> PIECE_WEIGHTS = Map.of(
        Piece.PAWN_ALGEBRAIC, 100,
        Piece.KNIGHT_ALGEBRAIC, 280,
        Piece.BISHOP_ALGEBRAIC, 320,
        Piece.ROOK_ALGEBRAIC, 479,
        Piece.QUEEN_ALGEBRAIC, 929,
        Piece.KING_ALGEBRAIC, 60000
    );

    protected static final int[] PAWN_TABLE = {
        100, 100, 100, 100, 105, 100, 100, 100,
        78, 83, 86, 73, 102, 82, 85, 90,
        7, 29, 21, 44, 40, 31, 44, 7,
        -17, 16, -2, 15, 14, 0, 15, -13,
        -26, 3, 10, 9, 6, 1, 0, -23,
        -22, 9, 5, -11, -10, -2, 3, -19,
        -31, 8, -7, -37, -36, -14, 3, -31,
        0, 0, 0, 0, 0, 0, 0, 0,
    };

    protected static final int[] KNIGHT_TABLE = {
        -66, -53, -75, -75, -10, -55, -58, -70,
        -3, -6, 100, -36, 4, 62, -4, -14,
        10, 67, 1, 74, 73, 27, 62, -2,
        24, 24, 45, 37, 33, 41, 25, 17,
        -1, 5, 31, 21, 22, 35, 2, 0,
        -18, 10, 13, 22, 18, 15, 11, -14,
        -23, -15, 2, 0, 2, 0, -23, -20,
        -74, -23, -26, -24, -19, -35, -22, -69,
    };

    protected static final int[] BISHOP_TABLE = {
        -59, -78, -82, -76, -23, -107, -37, -50,
        -11, 20, 35, -42, -39, 31, 2, -22,
        -9, 39, -32, 41, 52, -10, 28, -14,
        25, 17, 20, 34, 26, 25, 15, 10,
        13, 10, 17, 23, 17, 16, 0, 7,
        14, 25, 24, 15, 8, 25, 20, 15,
        19, 20, 11, 6, 7, 6, 20, 16,
        -7, 2, -15, -12, -14, -15, -10, -10,
    };

    protected static final int[] ROOK_TABLE = {
        35, 29, 33, 4, 37, 33, 56, 50,
        55, 29, 56, 67, 55, 62, 34, 60,
        19, 35, 28, 33, 45, 27, 25, 15,
        0, 5, 16, 13, 18, -4, -9, -6,
        -28, -35, -16, -21, -13, -29, -46, -30,
        -42, -28, -42, -25, -25, -35, -26, -46,
        -53, -38, -31, -26, -29, -43, -44, -53,
        -30, -24, -18, 5, -2, -18, -31, -32,
    };

    protected static final int[] QUEEN_TABLE = {
        4, 54, 47, -99, -99, 60, 83, -62,
        -32, 10, 55, 56, 56, 55, 10, 3,
        -62, 12, -57, 44, -67, 28, 37, -31,
        -55, 50, 11, -4, -19, 13, 0, -49,
        -55, -43, -52, -28, -51, -47, -8, -50,
        -47, -42, -43, -79, -64, -32, -29, -32,
        -4, 3, -14, -50, -57, -18, 13, 4,
        17, 30, -3, -14, 6, -1, 40, 18,
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
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50,
    };

    protected Move currentMove;
    protected Piece destinationPiece;

    @Override
    protected void initializePSTs() {
        PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(reverse(KING_MIDGAME_TABLE));
        PieceSquareTables[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        PieceSquareTables[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(reverse(QUEEN_TABLE));
        PieceSquareTables[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        PieceSquareTables[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(reverse(BISHOP_TABLE));
        PieceSquareTables[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        PieceSquareTables[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(reverse(KNIGHT_TABLE));
        PieceSquareTables[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        PieceSquareTables[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(reverse(ROOK_TABLE));
        PieceSquareTables[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        PieceSquareTables[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(reverse(PAWN_TABLE));
    }

    public ChessAIPlayer(PlayerColor color) {
        super(color);
    }

    @Override
    public Move getNextMove(Game game) {
        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        Collections.shuffle(moves); // Sort moves randomly, so the same move isn't always picked on ties

        int bestEvaluation = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        evaluatingMoves.clear();

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            currentMove = move;
            destinationPiece = game.getBoard().get(move.getTo());
            game.move(move);
            nodesEvaluated++;

            // alphaBetaMax(-oo, +oo, depth);
            int score = alphaBetaMax(game, Integer.MIN_VALUE, Integer.MAX_VALUE, searchDepth - 1);
            game.undo();
            evaluatingMoves.pollLast();

            // LOGGER.info("Move ({}): {}", move, score);

            if(score == bestEvaluation) {
                bestMoves.add(move);
            } else if(score > bestEvaluation) {
                bestMoves.clear();
                bestMoves.add(move);
                bestEvaluation = score;
            }
        }

        assert !bestMoves.isEmpty();
        this.bestMoveScore = bestEvaluation;

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.

        return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
    }


    /**
     * https://www.chessprogramming.org/Alpha-Beta#Implementation
     *
     * int alphaBetaMax( int alpha, int beta, int depthleft ) {
     *    if ( depthleft == 0 ) return evaluate();
     *    for ( all moves) {
     *       score = alphaBetaMin( alpha, beta, depthleft - 1 );
     *       if( score >= beta )
     *          return beta;   // fail hard beta-cutoff
     *       if( score > alpha )
     *          alpha = score; // alpha acts like max in MiniMax
     *    }
     *    return alpha;
     * }
     *
     * int alphaBetaMin( int alpha, int beta, int depthleft ) {
     *    if ( depthleft == 0 ) return -evaluate();
     *    for ( all moves) {
     *       score = alphaBetaMax( alpha, beta, depthleft - 1 );
     *       if( score <= alpha )
     *          return alpha; // fail hard alpha-cutoff
     *       if( score < beta )
     *          beta = score; // beta acts like min in MiniMax
     *    }
     *    return beta;
     * }
     *
     * from the root:
     * score = alphaBetaMax(-oo, +oo, depth);
     *
     * NOTE: the algos on chessprogramming.org are very terse.  I believe depthleft incorporates a) the literal
     *       depth of the search, and also when there a no moves.
     *
     * @param game
     * @return
     */
    private int alphaBetaMax(Game game, int alpha, int beta, int depthleft ) {
        if ( depthleft == 0 ) return evaluate(game);

        MoveList<Move> moves = game.generateMoves();
        Collections.shuffle(moves); // Sort moves randomly, so the same move isn't always picked on ties

        int score;

        if(moves.isEmpty())
            return evaluate(game);

        for ( var move: moves ) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            nodesEvaluated++;

            score = alphaBetaMin( game, alpha, beta, depthleft - 1 );
            game.undo();
            evaluatingMoves.pollLast();

            if( score >= beta )
                return beta;   // fail hard beta-cutoff
            if( score > alpha )
                alpha = score; // alpha acts like max in MiniMax
        }

        return alpha;
    }

    private int alphaBetaMin( Game game, int alpha, int beta, int depthleft ) {
        if ( depthleft == 0 ) return -evaluate(game);

        MoveList<Move> moves = game.generateMoves();
        Collections.shuffle(moves);  // Sort moves randomly, so the same move isn't always picked on ties
        int score;

        if(moves.isEmpty())
            return -evaluate(game);

        for ( var move: moves ) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            nodesEvaluated++;

            score = alphaBetaMax( game, alpha, beta, depthleft - 1 );
            game.undo();
            evaluatingMoves.pollLast();

            if( score <= alpha )
                return alpha; // fail hard alpha-cutoff
            if( score < beta )
                beta = score; // beta acts like min in MiniMax
        }

        return beta;
    }

    // evaluation function always from White's perspective.
    protected int getSideToMove(Game game) {
        return game.getActivePlayerColor().isWhite() ? -1 : 1;
    }

    public int evaluate(Game game) {
        int bonus = 0;
        int sideMultiplier = getSideToMove(game);

        if(game.isCheckmated()) {
            return getSideToMove(game) == -1 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        if(game.isDraw() || game.isRepetition() || game.isStalemate())
            return 0;

        if(game.isChecked()) {
            bonus += 50 * sideMultiplier;
        }

        if(currentMove.isCapture()) {
            bonus += PIECE_WEIGHTS.get(destinationPiece.getAlgebraicChar()) +
                PieceSquareTables[destinationPiece.getIndex()][currentMove.getTo()] * sideMultiplier;
        }

        int boardScore = boardScore(game);

        // return (materialScore + mobilityScore + bonus + boardScore(game)) * sideToMove;
        bonus += boardScore;

        return bonus * sideMultiplier;
    }
}

