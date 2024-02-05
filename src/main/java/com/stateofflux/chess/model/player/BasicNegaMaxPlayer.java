package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/*
 * Negamax search is a variant form of minimax search that relies on the zero-sum property of a
 * two-player game.
 *
 * This algorithm relies on the fact that  min(a,b) = −max(−b,−a)  simplify the implementation
 * of the minimax algorithm. More precisely, the value of a position to player A in such a
 * game is the negation of the value to player B.
 *
 * The negamax search objective is to find the node score value for the player who is playing
 * at the root node.  This class assumes we're evaluating for THIS player's perspective.
 *
 * https://en.wikipedia.org/wiki/Negamax
*/
public class BasicNegaMaxPlayer extends Player {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNegaMaxPlayer.class);

    protected int bestMoveScore = 0;

    // In this implementation, this value is always calculated from the point
    // of view of player A, whose color value is one. In other words, higher
    // heuristic values always represent situations more favorable for white.
    protected final Evaluator evaluator;

    protected Comparator<Move> moveComparator;

    public BasicNegaMaxPlayer(PlayerColor pc, Evaluator evaluator) {
        super(pc, evaluator);
        this.searchDepth = DEFAULT_SEARCH_DEPTH;
        this.evaluator = evaluator;
    }

    protected Comparator<Move> getComparator() {
        if(this.moveComparator == null)
            this.moveComparator = new CaptureOnlyMoveComparator();

        return moveComparator;
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        moves.sort(getComparator());

        int max = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();


        for(Move move: moves) {
            game.move(move);
            int score = -negaMax(game, searchDepth - 1, color.otherColor());
            // LOGGER.info("score for move ({}) : {}", move, score);
            game.undo();
            // LOGGER.info("{} - move ({}): {}", game.getActivePlayer(), move, score);

            if(score == max) {
                bestMoves.add(move);
            } else if(score > max) {
                bestMoves.clear();
                bestMoves.add(move);
                max = score;
            }
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        Collections.shuffle(bestMoves);
        Move bestMove = bestMoves.get(0);
        this.bestMoveScore = max;

        // LOGGER.info("{}: {}: best move is {}, score {}", searchDepth, game.asFen(), bestMove, max);

        return bestMove;
    }

    /*
     * https://www.chessprogramming.org/Negamax
     *
     *   int negaMax( int depth ) {
     *       if ( depth == 0 ) return evaluate();
     *       int max = -oo;
     *       for ( all moves)  {
     *           score = -negaMax( depth - 1 );
     *           if( score > max )
     *               max = score;
     *       }
     *       return max;
     *   }
     */
    protected int negaMax(Game game, int depth, PlayerColor pc) {
        int sideToMove = pc.isWhite() ? 1 : -1;

        if(depth == 0)
            return evaluate(game, depth) * sideToMove;

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game, depth) * sideToMove;  // I want this evaluated from the perspective of the last turn so flip the color.

        int max = Integer.MIN_VALUE;
        int score;

        for(Move move: moves) {
            game.move(move);
            score = -negaMax(game,depth - 1, pc.otherColor());

            game.undo();

            if(score > max) {
                max = score;
            }
        }

        // LOGGER.info("negamax bestmove at depth {}: {}", depth, bestMove);
        return max;
    }

    public int getBestMoveScore() {
        return bestMoveScore;
    }
}
