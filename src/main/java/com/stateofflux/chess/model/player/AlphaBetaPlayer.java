package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AlphaBetaPlayer extends BasicNegaMaxPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaBetaPlayer.class);


    public AlphaBetaPlayer(PlayerColor color, Evaluator evaluator) {
        super(color, evaluator);
    }

    @Override
    public Move getNextMove(Game game) {
        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        moves.sort(moveComparator);

        int depth = searchDepth;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        PlayerColor pc = this.getColor();

        List<Move> bestMoves = new ArrayList<>();
        int value = Integer.MIN_VALUE;
        int score;

        for(Move move: moves) {
            game.move(move);
            score = -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor());
            game.undo();

            if(score == value) {
                bestMoves.add(move);
            } else if(score > value) {
                bestMoves.clear();
                bestMoves.add(move);
                value = score;
            }

            if(value > alpha)
                alpha = value;

            // at the root beta is always MAX, and therefore alpha can never be greater than MAX.
            // this condition really not applicable at this level, but leaving for symmetry with alphaBeta()
            if( alpha >= beta) {
                break;  // cut off
            }
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.

        return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
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
        int sideToMove = pc.isWhite() ? 1 : -1;

        if(depth == 0)
            return evaluate(game, pc) * sideToMove;

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game, depth) * sideToMove;

        moves.sort(moveComparator);

        int value = Integer.MIN_VALUE;

        for(Move move: moves) {
            game.move(move);
            value = Math.max(value, -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor()));
            game.undo();

            alpha = Math.max(alpha, value);

            if(alpha >= beta)
                break;
        }

        return value;
    }
}

