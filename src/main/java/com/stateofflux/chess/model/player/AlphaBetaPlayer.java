package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AlphaBetaPlayer extends BasicNegaMaxPlayer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public AlphaBetaPlayer(PlayerColor color, Evaluator evaluator) {
        super(color, evaluator);
    }

    @Override
    public Move getNextMove(Game game) {
        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        List<Move> bestMoves = new ArrayList<>();

        moves.sort(getComparator());

        // LOGGER.info("depth remaining: {}.  reviewing moves ({})", getSearchDepth(), moves);

        int depth = getSearchDepth();
        int alpha = Evaluator.MIN_VALUE;
        int beta = Evaluator.MAX_VALUE;
        PlayerColor pc = this.getColor();

        int value = Evaluator.MIN_VALUE;

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

            int score = -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor());
            game.undo();

            if(score == value) {
                bestMoves.add(move);
            } else if(score > value) {
                bestMoves.clear();
                bestMoves.add(move);
                value = score;
            }

            if(value > alpha) {
                alpha = value;
            }

            // at the root beta is always MAX, and therefore alpha can never be greater than MAX.
            // this condition really not applicable at this level, but leaving for symmetry with alphaBeta()
            if( alpha >= beta) {
                // LOGGER.info("depth remaining: {}. Cut off: α {}, β {}", depth, alpha, beta);
                break;  // cut off
            }
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        Move bestMove = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));

        logger.atDebug().log("{} : depth remaining(α {}, β {}): {}.  best move: {} ({})",
            game.getActivePlayerColor().isWhite() ? "MAX" : "MIN",
            alpha,
            beta,
            depth,
            bestMove,
            value);

        return bestMove;
    }

    /*
     * NegaMax with alpha-beta pruning.  https://en.wikipedia.org/wiki/Negamax#Negamax_with_alpha_beta_pruning
     *
     * function negamax(node, depth, α, β, color) is
     *    if depth = 0 or node is a terminal node then
     *        return color × the heuristic value of node
     *
     *    childNodes := generateMoves(node)
     *    childNodes := orderMoves(childNodes)
     *    value := −∞
     *    foreach child in childNodes do
     *        value := max(value, −negamax(child, depth − 1, −β, −α, −color))
     *        α := max(α, value)
     *        if α ≥ β then
     *            break (* cut-off *)
     *    return value
     *
     * (* Initial call for Player A's root node *)
     *    negamax(rootNode, depth, −∞, +∞, 1)
     */
    public int alphaBeta(Game game, int depth, int alpha, int beta, PlayerColor pc) {
        assert pc == game.getActivePlayerColor();

        if(depth == 0)
            return evaluate(game, getSearchDepth() - depth);

        MoveList<Move> moves = game.generateMoves();
        List<Move> bestMoves = new ArrayList<>();

        // node is terminal
        if(moves.isEmpty())  // mate, draw, etc.
            return evaluate(game, getSearchDepth() - depth);

        moves.sort(getComparator());

        // logger.info("depth remaining: {}.  reviewing moves ({})", depth, moves);

        int value = Evaluator.MIN_VALUE;

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

            int score = -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor());
            game.undo();

            if(score == value) {
                bestMoves.add(move);
            } else if(score > value) {
                bestMoves.clear();
                bestMoves.add(move);
                value = score;
            }

            if(value >= alpha) {
                alpha = value;
            }

            if(alpha >= beta) {
                // LOGGER.info("depth remaining: {}. Cut off: α {}, β {}", depth, alpha, beta);
                break;
            }
        }

        Move bestMove = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));

        logger.atDebug().log("{}{} : depth: {} (α {}, β {}).  best move: {} ({})",
            " ".repeat((getSearchDepth() - depth) * 2),
            game.getActivePlayerColor().isWhite() ? "MAX" : "MIN",
            depth,
            alpha,
            beta,
            bestMove,
            value);

        return value;
    }
}

