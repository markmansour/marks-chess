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

    protected Move currentMove;
    protected Piece destinationPiece;

    public ChessAIPlayer(PlayerColor color, Evaluator evaluator) {
        super(color, evaluator);
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
            int currentScore = evaluate(game, this.getColor());
            game.move(move);

            // alphaBetaMax(-oo, +oo, depth);
            int score = alphaBetaMax(game, Integer.MIN_VALUE, Integer.MAX_VALUE, searchDepth - 1, this.getColor());
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
    private int alphaBetaMax(Game game, int alpha, int beta, int depthleft, PlayerColor pc ) {
        if ( depthleft == 0 ) return evaluate(game, pc);

        MoveList<Move> moves = game.generateMoves();
        Collections.shuffle(moves); // Sort moves randomly, so the same move isn't always picked on ties

        int score;

        if(moves.isEmpty())
            return evaluate(game, pc);

        for ( var move: moves ) {
            evaluatingMoves.offerLast(move);
            game.move(move);

            score = alphaBetaMin( game, alpha, beta, depthleft - 1, pc.otherColor() );
            game.undo();
            evaluatingMoves.pollLast();

            if( score >= beta )
                return beta;   // fail hard beta-cutoff
            if( score > alpha )
                alpha = score; // alpha acts like max in MiniMax
        }

        return alpha;
    }

    private int alphaBetaMin( Game game, int alpha, int beta, int depthleft, PlayerColor pc ) {
        if ( depthleft == 0 ) return -evaluate(game, pc);

        MoveList<Move> moves = game.generateMoves();
        Collections.shuffle(moves);  // Sort moves randomly, so the same move isn't always picked on ties
        int score;

        if(moves.isEmpty())
            return -evaluate(game, pc);

        for ( var move: moves ) {
            evaluatingMoves.offerLast(move);
            game.move(move);

            score = alphaBetaMax( game, alpha, beta, depthleft - 1, pc.otherColor() );
            game.undo();
            evaluatingMoves.pollLast();

            if( score <= alpha )
                return alpha; // fail hard alpha-cutoff
            if( score < beta )
                beta = score; // beta acts like min in MiniMax
        }

        return beta;
    }


}

