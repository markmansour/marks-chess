package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AlphaBetaPlayer extends BasicNegaMaxPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaBetaPlayer.class);

    public AlphaBetaPlayer(PlayerColor color) {
        super(color);
    }

    @Override
    public Move getNextMove(Game game) {
        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        int bestEvaluation = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        evaluatingMoves.clear();

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
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
}

