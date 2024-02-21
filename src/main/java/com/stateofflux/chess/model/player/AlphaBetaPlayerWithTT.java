package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.stateofflux.chess.App.uci_logger;

public class AlphaBetaPlayerWithTT extends BasicNegaMaxPlayer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long DEFAULT_TIME_ALLOCATION = TimeUnit.MINUTES.toNanos(5);
    private static final long DEFAULT_INCREMENT_ALLOCATION = TimeUnit.SECONDS.toNanos(5);

    private TranspositionTable tt;
    private int tableHits;
    private final Timer timer;
    private long increment;
    private boolean timedOut;

    public AlphaBetaPlayerWithTT(PlayerColor color, Evaluator evaluator) {
        this(color, evaluator, DEFAULT_TIME_ALLOCATION);
    }

    public AlphaBetaPlayerWithTT(PlayerColor color, Evaluator evaluator, long timeAllocatedForPlayer) {
        super(color, evaluator);
        tt = new TranspositionTable();
        tableHits = 0;
        timer = Timer.create(timeAllocatedForPlayer);
        timedOut = false;
    }

    @Override
    protected Comparator<Move> getComparator() {
        if(this.moveComparator == null)
            this.moveComparator = new MvvLvaMoveComparator();

        return moveComparator;
    }

    // in Nanos.
    @Override
    public void setIncrement(long increment) {
        this.increment = increment;
    }

    // in Nanos.
    public long getIncrement() {
        return (this.increment == 0) ? DEFAULT_INCREMENT_ALLOCATION : this.increment;
    }

    @Override
    public void reset() {
        super.reset();
        tableHits = 0;
        tt.clear();
    }

    @Override
    public Move getNextMove(Game game) {
        reset();
        timer.startIncrementCountdown(getIncrement());
        timedOut = false;
        int searchDepth = getSearchDepth();
        MoveHistory bestMove = null;

        uci_logger.atInfo().log("info string depth set to {}; increment set to {}ms", searchDepth, TimeUnit.NANOSECONDS.toMillis(getIncrement()));

        // Iterative Deepening loop
        for(int depth = 1; depth <= searchDepth && !timedOut; depth++) {
            bestMove = alphaBetaRoot(game, depth);
            logger.atDebug().log("{} - tt cache hits: {}.  TT {}/{} ({}%)", depth, getTableHits(), getTtEntries(), getTtHashSize(), (int) (((double) getTtEntries()) / (double) getTtHashSize() * 100));
            logger.atDebug().log("best move: {}.  Node visited: {}", bestMove, getNodesVisited());

            // info depth 10 seldepth 14 multipv 1 score cp 27 nodes 144979 nps 2163865 hashfull 54 tbhits 0 time 67 pv e2e4 d7d5 e4d5 g8f6 g1f3 d8d5 b1c3 d5e6 d1e2
            uci_logger.atInfo().log("info depth {} score {} nodes {} nps {} hashfull {} time {} pv {} {}",
                depth,
                bestMove.score(),
                getNodesVisited(),
                getNodesVisited() * 1000L / (TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()) + 1),
                tt.getHashfull(),
                TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                bestMove.bestMove().toLongSan(),
                bestMove.previousMovesToString()
                );
        }

        return bestMove.bestMove();
    }

    public MoveHistory alphaBetaRoot(Game game, int depth) {
        int alpha = Evaluator.MIN_VALUE;
        int beta = Evaluator.MAX_VALUE;
        PlayerColor pc = this.getColor();
        int alphaOrig = alpha;
        int value = Evaluator.MIN_VALUE;
        List<MoveHistory> bestMoves = new ArrayList<>();
        int score;

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());

        if(existingEntry != null && existingEntry.depth() >= getSearchDepth()) {
            tableHits++;

            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                return new MoveHistory(existingEntry.getBestMove(), existingEntry.score());  // TODO: we're not storing history to retrieve (yet!)
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                logger.atDebug().log("returning cached hit - a >= b");
                return new MoveHistory(existingEntry.getBestMove(), existingEntry.score());  // TODO: we're not storing history to retrieve (yet!)
            }
        }

        MoveList<Move> moves = game.generateMoves();
        moves.sort(getComparator());
        MoveHistory mh;

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

            mh = alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor(), move);
            score = -mh.score();
            game.undo();

            if(score == value) {
                bestMoves.add(mh);
            } else if(score > value) {
                bestMoves.clear();
                mh.addMove(move, score);
                bestMoves.add(mh);
                value = score;
            }

            if(value > alpha)
                alpha = value;

            // at the root beta is always MAX, and therefore alpha can never be greater than MAX.
            // this condition really not applicable at this level, but leaving for symmetry with alphaBeta()
            if( alpha >= beta) {
                break;  // cut off
            }

            if(timedOut && !bestMoves.isEmpty()) {
                break;
            }
        }

        assert !bestMoves.isEmpty();

        MoveHistory bestMoveHistory = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
        Move bestMove = bestMoveHistory.bestMove();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        updateTranspositionTable(game, value, bestMove, alphaOrig, beta, depth);

        logger.atDebug().log("{} : depth remaining {}, (α {}, β {}). best move: {} ({})",
            game.getActivePlayerColor().isWhite() ? "W" : "B",
            depth,
            alpha,
            beta,
            bestMove,
            value);

        return bestMoveHistory;
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
    public MoveHistory alphaBeta(Game game, int depth, int alpha, int beta, PlayerColor pc, Move lastMove) {
        assert game.getActivePlayerColor() == pc;

        int alphaOrig = alpha;

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());
        if(existingEntry != null && existingEntry.depth() >= depth) {
            tableHits++;

            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                return new MoveHistory(existingEntry.getBestMove(), existingEntry.score());  // TODO: we're not storing history to retrieve (yet!)
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                return new MoveHistory(existingEntry.getBestMove(), existingEntry.score());  // TODO: we're not storing history to retrieve (yet!)
            }
        }

        int sideMoved = pc.isWhite() ? 1 : -1;

        if(depth == 0) {
            int evaluatedScore = evaluate(game, pc) * sideMoved;
            return new MoveHistory(lastMove, evaluatedScore);  // TODO: we're not storing history to retrieve (yet!)
        }

        MoveList<Move> moves = game.generateMoves();

        // node is terminal as there are no more moves.
        if(moves.isEmpty()) {
            return new MoveHistory(
                lastMove,
                evaluate(game, getSearchDepth() - depth) * sideMoved
            );
        }

        moves.sort(getComparator());

        int value = Evaluator.MIN_VALUE;
        List<MoveHistory> bestMoves = new ArrayList<>();
        int score;
        MoveHistory mh;
        int evaluatedCount = 0;

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

            // are we out of time?  Every 4096 nodes, check to see if we're out of time.
            // Using bit manipulation to check for modulo (numerator & (denominator - 1)) == 0.
            if((nodesVisited & 4095) == 0 && moves.size() != 1 && timer.incrementIsUsed()) {
                logger.atDebug().log("timing out after {}ms (allocation of {}ms)",
                    TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                    TimeUnit.NANOSECONDS.toMillis(timer.getIncrementAllocation()));
                timedOut = true;
            }

            // even if we've timed out, keep going one more time to ensure we have a best move as the cost
            // of an extra iteration should be cheap
            if(timedOut && !bestMoves.isEmpty()) {
                game.undo();
                break;
            }

            mh = alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor(), move);
            score = -mh.score();
            evaluatedCount++;
            game.undo();

            if(score == value) {
                bestMoves.add(mh);
            } else if(score > value) {
                bestMoves.clear();
                if(depth == 1)
                    bestMoves.add(mh);  // leaf node
                else {
                    mh.addMove(move, score);
                    bestMoves.add(mh);
                }

                value = score;
            }

            if(value > alpha)
                alpha = value;

            if(alpha >= beta)
                break;
        }

/*
        if(bestMoves.isEmpty()) {
            logger.atDebug().log("search has been terminated but without a best value at this level");
            // return Evaluator.MAX_VALUE;  // the aim is that this is a no-op as no best value has been found.
            return mh.createNew(null, Evaluator.MAX_VALUE);
        }
*/

        MoveHistory bestMoveHistory = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
        Move bestMove = bestMoveHistory.bestMove();

        updateTranspositionTable(game, value, bestMove, alphaOrig, beta, depth);

        logger.atDebug().log("{} : depth remaining {}, (α {}, β {}). pruned #: {}/{}. best move: {} ({})",
            // " ".repeat((getSearchDepth() - depth) * 2),   // for some reason this line breaks logging!
            game.getActivePlayerColor().isWhite() ? "W" : "B",
            depth,
            alpha,
            beta,
            moves.size() - evaluatedCount,
            moves.size(),
            bestMove,
            value);

        return bestMoveHistory;
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

    public int getTableHits() {
        return tableHits;
    }

    public int getTtEntries() {
        return tt.getActiveEntries();
    }

    public int getTtHashSize() {
        return tt.getMaxEntries();
    }

    @Override
    public void setHashInMb(int hashSize) {
        logger.atDebug().log("resizing hash to {} mb", hashSize);
        tt = new TranspositionTable(hashSize);
    }
}

