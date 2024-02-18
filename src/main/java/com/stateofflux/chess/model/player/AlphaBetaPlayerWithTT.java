package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class AlphaBetaPlayerWithTT extends BasicNegaMaxPlayer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long DEFAULT_TIME_ALLOCATION = TimeUnit.MINUTES.toNanos(5);
    private static final long DEFAULT_INCREMENT_ALLOCATION = TimeUnit.SECONDS.toNanos(5);

    private TranspositionTable tt;
    private int tableHits;
    private final Timer timer;
    private long increment;
    private boolean timedOut;
    final static Deque<Move> moveHistory = new ArrayDeque<>();

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
    public Move getNextMove(Game game) {
        timer.startIncrementCountdown(getIncrement());
        timedOut = false;
        int searchDepth = getSearchDepth();
        Move bestMove = null;

        for(int depth = 1; depth <= searchDepth && !timedOut; depth++) {
            bestMove = alphaBetaRoot(game, depth);
            logger.info("{} - tt cache hits: {}.  TT {}/{} ({}%)", depth, getTableHits(), getTtEntries(), getTtHashSize(), (int) (((double) getTtEntries()) / (double) getTtHashSize() * 100));
            logger.info("best move: {}.  Node visited: {}", bestMove, getNodesVisited());
        }

        return bestMove;
    }

    public Move alphaBetaRoot(Game game, int depth) {
        int alpha = Evaluator.MIN_VALUE;
        int beta = Evaluator.MAX_VALUE;
        PlayerColor pc = this.getColor();
        int alphaOrig = alpha;
        int value = Evaluator.MIN_VALUE;
        int score;
        List<Move> bestMoves = new ArrayList<>();

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());

        if(existingEntry != null && existingEntry.depth() >= getSearchDepth()) {
            tableHits++;
            // LOGGER.info("TT hit at root");

            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                // LOGGER.info("returning cached hit - EXACT");
                return existingEntry.getBestMove();
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                logger.info("returning cached hit - a >= b");
                return existingEntry.getBestMove();
            }
        }

        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());
        MoveList<Move> moves = game.generateMoves();
        moves.sort(getComparator());

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

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

            if(timedOut) {
                break;
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
            tableHits++;
            // LOGGER.info("TT hit");
            if(existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                // LOGGER.info("returning cached hit - EXACT");
                return existingEntry.score();
            } else if(existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND)
                alpha = Math.max(alpha, existingEntry.score());
            else if(existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if(alpha >= beta) {
                // LOGGER.info("returning cached hit - a >= b");
                return existingEntry.score();
            }
        }

        int sideMoved = pc.isWhite() ? 1 : -1;

        if(depth == 0)
            return evaluate(game, pc) * sideMoved;

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game, getSearchDepth() - depth) * sideMoved;

        moves.sort(getComparator());

        int value = Evaluator.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        int score;

        for(Move move: moves) {
            game.move(move);
            nodesVisited++;

            // are we out of time?
            if((nodesVisited & 4095) == 0 && moves.size() != 1) {  // every 4096 nodes, check to see if we're out of time: (numerator & (denominator - 1)) == 0.
                if(timer.incrementIsUsed()) {
                    logger.atInfo().log("timing out after {}ms (allocation of {}ms)",
                        TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                        TimeUnit.NANOSECONDS.toMillis(timer.getIncrementAllocation()));
                    timedOut = true;
                }
            }

            if(timedOut) {
                game.undo();
                // score = Evaluator.MIN_VALUE;
                break;
            }

            score = -alphaBeta(game,depth - 1, -beta, -alpha, pc.otherColor());
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

            if(alpha >= beta) {
                // LOGGER.info("depth remaining: {}. Cut off: α {}, β {}", depth, alpha, beta);
                break;
            }
        }

        if(bestMoves.isEmpty()) {
            logger.atInfo().log("search has been terminated but without a best value at this level");
            return Evaluator.MAX_VALUE;  // the aim is that this is a no-op as no best value has been found.
            // return 0;
        }

        Move bestMove = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));

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

    public int getTableHits() {
        return tableHits;
    }

    public int getTtEntries() {
        return tt.getEntryCount();
    }

    public int getTtHashSize() {
        return tt.getHashSize();
    }

    @Override
    public void setHashInMb(int hashSize) {
        logger.atInfo().log("resizing hash to {} mb", hashSize);
        tt = new TranspositionTable(hashSize);
    }
}

