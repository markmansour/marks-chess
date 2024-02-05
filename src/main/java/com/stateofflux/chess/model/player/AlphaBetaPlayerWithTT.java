package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AlphaBetaPlayerWithTT extends BasicNegaMaxPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaBetaPlayerWithTT.class);
    private TranspositionTable tt;

    public AlphaBetaPlayerWithTT(PlayerColor color, Evaluator evaluator) {
        super(color, evaluator);
        tt = new TranspositionTable();
    }

    @Override
    protected Comparator<Move> getComparator() {
        if(this.moveComparator == null)
            this.moveComparator = new MvvLvaMoveComparator();

        return moveComparator;
    }

    @Override
    public Move getNextMove(Game game) {
        int depth = searchDepth;
        int alpha = Integer.MIN_VALUE;
        int alphaOrig = alpha;
        int beta = Integer.MAX_VALUE;
        PlayerColor pc = this.getColor();
        int value = Integer.MIN_VALUE;
        int score;
        List<Move> bestMoves = new ArrayList<>();

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());
        if(existingEntry != null && existingEntry.depth() >= depth) {
            LOGGER.info("TT hit at root");
            // TODO: write logic to reassemble a move.
        }

        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        moves.sort(getComparator());


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

        updateTranspositionTable(game, value, alphaOrig, beta, depth);

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.

        return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
    }

    private void updateTranspositionTable(Game game, int value, int alphaOrig, int beta, int depth) {
        TranspositionTable.NodeType nt;
        if( value <= alphaOrig)
            nt = TranspositionTable.NodeType.UPPER_BOUND;
        else if(value >= beta)
            nt = TranspositionTable.NodeType.LOWER_BOUND;
        else
            nt = TranspositionTable.NodeType.EXACT;

        TranspositionTable.Entry newEntry = new TranspositionTable.Entry(game.getZobristKey(), null, depth, value, nt, 0);
        tt.put(game.getZobristKey(), value, depth, nt, game.getClock());
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
        int alphaOrig = alpha;

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());
        if(existingEntry != null && existingEntry.depth() >= depth) {
            LOGGER.info("TT hit");
            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT)
                return existingEntry.score();
            else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta)
                return existingEntry.score();
        }

        int sideToMove = pc.isWhite() ? 1 : -1;

        if(depth == 0)
            return evaluate(game, pc) * sideToMove;

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game, depth) * sideToMove;

        moves.sort(getComparator());

        int value = Integer.MIN_VALUE;

        for(Move move: moves) {
            game.move(move);
            value = Math.max(value, -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor()));
            game.undo();

            alpha = Math.max(alpha, value);

            if(alpha >= beta)
                break;
        }

        updateTranspositionTable(game, value, alphaOrig, beta, depth);

        return value;
    }
}

