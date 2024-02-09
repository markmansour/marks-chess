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
    private final TranspositionTable tt;

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
        int depth = getSearchDepth();
        int alpha = Integer.MIN_VALUE;
        int alphaOrig = alpha;
        int beta = Integer.MAX_VALUE;
        PlayerColor pc = this.getColor();
        int value = Integer.MIN_VALUE;
        int score;
        List<Move> bestMoves = new ArrayList<>();

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());

        if(existingEntry != null && existingEntry.depth() >= getSearchDepth()) {
            LOGGER.info("TT hit at root");

            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                LOGGER.info("returning cached hit - EXACT");
                return existingEntry.getBestMove();
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                LOGGER.info("returning cached hit - a >= b");
                return existingEntry.getBestMove();
            }
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


        assert !bestMoves.isEmpty();
        Move bestMove = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        updateTranspositionTable(game, value, bestMove, alphaOrig, beta, depth);

        return bestMove;
    }

    /*
     * The pseudocode that adds transposition table functions to negamax with alpha/beta pruning is given as follows
     *   -> https://en.wikipedia.org/wiki/Negamax
     *
     * function negamax(node, depth, α, β, color) is
     *     alphaOrig := α
     *
     *     (* Transposition Table Lookup; node is the lookup key for ttEntry *)
     *     ttEntry := transpositionTableLookup(node)
     *     if ttEntry is valid and ttEntry.depth ≥ depth then
     *         if ttEntry.flag = EXACT then
     *             return ttEntry.value
     *         else if ttEntry.flag = LOWERBOUND then
     *             α := max(α, ttEntry.value)
     *         else if ttEntry.flag = UPPERBOUND then
     *             β := min(β, ttEntry.value)
     *
     *         if α ≥ β then
     *             return ttEntry.value
     *
     *     if depth = 0 or node is a terminal node then
     *         return color × the heuristic value of node
     *
     *     childNodes := generateMoves(node)
     *     childNodes := orderMoves(childNodes)
     *     value := −∞
     *     for each child in childNodes do
     *         value := max(value, −negamax(child, depth − 1, −β, −α, −color))
     *         α := max(α, value)
     *         if α ≥ β then
     *             break
     *
     *     (* Transposition Table Store; node is the lookup key for ttEntry *)
     *     ttEntry.value := value
     *     if value ≤ alphaOrig then
     *         ttEntry.flag := UPPERBOUND
     *     else if value ≥ β then
     *         ttEntry.flag := LOWERBOUND
     *     else
     *         ttEntry.flag := EXACT
     *     ttEntry.depth := depth
     *     ttEntry.is_valid := true
     *     transpositionTableStore(node, ttEntry)
     *
     *     return value
     *
     * (* Initial call for Player A's root node *)
     *    negamax(rootNode, depth, −∞, +∞, 1)
     */
    public int alphaBeta(Game game, int depth, int alpha, int beta, PlayerColor pc) {
        int alphaOrig = alpha;

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());
        if(existingEntry != null && existingEntry.depth() >= depth) {
            LOGGER.info("TT hit");
            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                LOGGER.info("returning cached hit - EXACT");
                return existingEntry.score();
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                LOGGER.info("returning cached hit - a >= b");
                return existingEntry.score();
            }
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
        Move bestMove = null;

        for(Move move: moves) {
            game.move(move);

            int score = -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor());
            if(score > value) {
                value = score;
                bestMove = move;
            }

            game.undo();

            alpha = Math.max(alpha, value);

            if(alpha >= beta)
                break;
        }

        updateTranspositionTable(game, value, bestMove, alphaOrig, beta, depth);

        return value;
    }

    private void updateTranspositionTable(Game game, int value, Move best, int alphaOrig, int beta, int depth) {
        TranspositionTable.NodeType nt;
        if( value <= alphaOrig)
            nt = TranspositionTable.NodeType.UPPER_BOUND;
        else if(value >= beta)
            nt = TranspositionTable.NodeType.LOWER_BOUND;
        else
            nt = TranspositionTable.NodeType.EXACT;

        // TranspositionTable.Entry newEntry = new TranspositionTable.Entry(game.getZobristKey(), null, depth, value, nt, 0);
        tt.put(game.getZobristKey(), value, best, nt, depth, game.getClock());
    }
}

