package com.stateofflux.chess.model.player;

import com.google.common.graph.*;
import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

import static com.stateofflux.chess.model.pieces.KingMoves.MATE_VALUE;
import static java.lang.Long.bitCount;

/*
 * Negamax search is a variant form of minimax search that relies on the zero-sum property of a
 * two-player game.
 *
 * This algorithm relies on the fact that  min(a,b) = −max(−b,−a)  simplify the implementation
 * of the minimax algorithm.
 *
 * https://en.wikipedia.org/wiki/Negamax
*/
public class BasicNegaMaxPlayer extends Player {

    static class Node {
        private final Move move;
        private int score;
        public Node(Move move) {this.move = move; }
        public void setScore(int score) { this.score = score; }
        @Override public String toString() { return score + " - " + move.toString(); }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNegaMaxPlayer.class);

    protected Deque<Move> evaluatingMoves;
    protected int bestMoveScore = 0;
    protected Evaluator evaluator;
    protected MutableValueGraph<Move, Integer> evaluationTree;
    protected Move root;  // fake move as root

    public BasicNegaMaxPlayer(PlayerColor pc, Evaluator evaluator) {
        super(pc, evaluator);
        this.searchDepth = DEFAULT_SEARCH_DEPTH;
        this.evaluator = evaluator;
        evaluatingMoves = new ArrayDeque<Move>();
        root = new Move(Piece.EMPTY, 0, 0, false);
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        moves.sort(new MoveComparator());

        int max = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        evaluatingMoves.clear();
        evaluationTree = ValueGraphBuilder.directed().build();
        evaluationTree.addNode(root);

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            evaluationTree.addNode(move);
            int score = negaMax(game, searchDepth - 1, move);
            evaluationTree.putEdgeValue(root, move, score);
            game.undo();
            evaluatingMoves.pollLast();
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
    protected int negaMax(Game game, int depth, Move parentNode) {
        if(depth == 0) { // can the king count be 0 here?  Should we be checking?
            return evaluate(game);
        }
        int max = Integer.MIN_VALUE;
        int score;
        Move bestMove = null; // for debugging.

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game, evaluatingMoves.size());

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            evaluationTree.addNode(move);
            score = -negaMax(game,depth - 1, move);
            evaluationTree.putEdgeValue(parentNode, move, score);

            game.undo();
            evaluatingMoves.pollLast();

            if(score > max) {
                max = score;
                bestMove = move;
            }
        }

        // LOGGER.info("negamax bestmove at depth {}: {}", depth, bestMove);
        return max;
    }

    public int getBestMoveScore() {
        return bestMoveScore;
    }

    public Deque<Move> getEvaluatingMoves() {
        return evaluatingMoves;
    }
}
