package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.stateofflux.chess.App.uci_logger;

public class AlphaBetaPlayerWithTT extends BasicNegaMaxPlayer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    final static Logger xml = LoggerFactory.getLogger("com.stateofflux.chess.alpha-beta-debugging");

    private static final long DEFAULT_TIME_ALLOCATION = TimeUnit.MINUTES.toNanos(5);
    private static final long DEFAULT_INCREMENT_ALLOCATION = TimeUnit.SECONDS.toNanos(5);

    private TranspositionTable tt;
    private int tableHits;
    private final Timer timer;
    private long increment;
    private boolean timedOut;
    private Move lastMove;

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
        if (this.moveComparator == null)
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
        int maxDepth = getSearchDepth();
        List<Move> bestVariation = new ArrayList<>();

        // Set the ply unless the caller has already set the ply string
        if(xml.isDebugEnabled() && MDC.getCopyOfContextMap() != null && !MDC.getCopyOfContextMap().containsKey("ply"))
            MDC.put("ply", String.format("%03d", game.getClock()));

        uci_logger.atDebug().log("info string depth set to {}; increment set to {}ms", maxDepth, TimeUnit.NANOSECONDS.toMillis(getIncrement()));

        logger.atDebug().log("starting iterative deepening with fen: \"{}\"", game.asFen());

         xml.atDebug().log("<chess player=\"{}\" search-depth=\"{}\" fen-string=\"{}\">", game.getActivePlayerColor(), maxDepth, game.asFen());

        // Iterative Deepening loop
        for (int depth = 1; depth <= maxDepth && !timedOut; depth++) {
            xml.atDebug().log("<iteration depth=\"{}\">", depth);

            List<Move> currentVariation = new ArrayList<>();
            int score = alphaBeta(game, depth, Evaluator.MIN_VALUE, Evaluator.MAX_VALUE, currentVariation);

            // throw away timed out values.  Is there a way to use them?
            if (!currentVariation.isEmpty() && !timedOut) {
                bestVariation = new ArrayList<>(currentVariation);
                logger.atDebug().log("taking the new best variation of {}", bestVariation);
            } else {
                logger.atDebug().log("Timed out");
            }

            // info depth 10 seldepth 14 multipv 1 score cp 27 nodes 144979 nps 2163865 hashfull 54 tbhits 0 time 67 pv e2e4 d7d5 e4d5 g8f6 g1f3 d8d5 b1c3 d5e6 d1e2
            xml.atDebug().log("<search-summary>info depth {} score {} nodes {} nps {} hashfull {} time {} pv {}</search-summary>",
                depth,
                score,
                getNodesVisited(),
                getNodesVisited() * 1000L / (TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()) + 1),
                tt.getHashfull(),
                TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                currentVariation.stream().map(Move::toLongSan).collect(Collectors.joining(" "))
            );
            xml.atDebug().log("</iteration>");

            uci_logger.atInfo().log("info depth {} score {} nodes {} nps {} hashfull {} time {} pv {}",
                depth,
                score,
                getNodesVisited(),
                getNodesVisited() * 1000L / (TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()) + 1),
                tt.getHashfull(),
                TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                currentVariation.stream().map(Move::toLongSan).collect(Collectors.joining(" "))
            );
        }
        xml.atDebug().log("</chess>");

        if(xml.isDebugEnabled())
            MDC.remove("ply");

        logger.atDebug().log("best variation: {}", bestVariation);
        return bestVariation.get(0);
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
    public int alphaBeta(Game game, int depth, int alpha, int beta, List<Move> principalVariation) {
        int alphaOrig = alpha;

        TranspositionTable.Entry existingEntry = tt.get(game.getZobristKey(), game.getClock());
        if (existingEntry != null && existingEntry.depth() >= depth) {
            tableHits++;

            if (existingEntry.nt() == TranspositionTable.NodeType.EXACT) {
                return existingEntry.score();
            } else if (existingEntry.nt() == TranspositionTable.NodeType.LOWER_BOUND) {
                alpha = Math.max(alpha, existingEntry.score());
            } else if (existingEntry.nt() == TranspositionTable.NodeType.UPPER_BOUND)
                beta = Math.min(beta, existingEntry.score());

            if (alpha >= beta) {
                return existingEntry.score();
            }
        }

        /*
         * The player state will always be the next players turn.  This is because the previous call from alphabeta completed
         * a move(), which updates the board AND changes the current player.
         *
         * e.g. if white just moved, then the board knows it is black's turn.
         *
         * So when we call the evaluation function and white has just moved, it will give us a value for black's view
         * of the board.
         *
         * The resulting call to alphabeta flips the sign of the result, thus changing the evaluation results from
         * black's view back to white's view.
         *
         * So, if white just had its turn, and it is in a dominant position we want to return a negative number as it
         * will be flipped once the recursion unwinds.
         */
        if (depth == 0) {
            principalVariation.clear();
            int evaluatedScore = evaluate(game, getSearchDepth() - depth);

            xml.atDebug().log("<evaluate player=\"{}\" depth-remaining=\"{}\" alpha=\"{}\" beta=\"{}\" move=\"{}\" score=\"{}\"/>",
                game.getActivePlayerColor(),
                depth,
                alpha,
                beta,
                lastMove.toLongSan(),
                evaluatedScore
            );

            return evaluatedScore;
        }

        MoveList<Move> moves = game.generateMoves();

        // node is terminal as there are no more moves.
        if (moves.isEmpty()) {
            principalVariation.clear();
            int evaluatedScore = evaluate(game, getSearchDepth() - depth);

            xml.atDebug().log("<evaluate player=\"{}\" depth-remaining=\"{}\" alpha=\"{}\" beta=\"{}\" move=\"{}\" score=\"{}\"/>",
                game.getActivePlayerColor(),
                depth,
                alpha,
                beta,
                lastMove.toLongSan(),
                evaluatedScore
            );

            return evaluatedScore;
        }

        moves.sort(getComparator());

        int value = Evaluator.MIN_VALUE;
        List<List<Move>> bestVariations = new ArrayList<>();
        int evaluatedCount = 0;

        xml.atDebug().log("<node depth=\"{}\" move=\"{}\">", depth, lastMove == null ? "no-move-yet" : lastMove.toLongSan());

        for (Move move : moves) {
            game.move(move);
            lastMove = move;
            nodesVisited++;

            // are we out of time?  Every 4096 nodes, check to see if we're out of time.
            // Using bit manipulation to check for modulo (numerator & (denominator - 1)) == 0.
            if ((nodesVisited & 4095) == 0 && moves.size() != 1 && timer.incrementIsUsed()) {
                logger.atDebug().log("timing out after {}ms (allocation of {}ms)",
                    TimeUnit.NANOSECONDS.toMillis(timer.incrementTimeUsed()),
                    TimeUnit.NANOSECONDS.toMillis(timer.getIncrementAllocation()));
                timedOut = true;
            }

            // even if we've timed out, keep going one more time to ensure we have a best move as the cost
            // of an extra iteration should be cheap
            if (timedOut && !bestVariations.isEmpty()) {
                game.undo();
                break;
            }

            List<Move> childVariation = new ArrayList<>();

            int score = -alphaBeta(game, depth - 1, -beta, -alpha, childVariation);
            evaluatedCount++;
            game.undo();

            if (score == value) {
                childVariation.add(0, move);
                bestVariations.add(new ArrayList<>(childVariation));
            } else if (score > value) {
                value = score;

                bestVariations.clear();
                childVariation.add(0, move);
                bestVariations.add(new ArrayList<>(childVariation));
            }

            alpha = Math.max(alpha, value);

            if (alpha >= beta)
                break;  // Alpha-beta cutoff
        }

        principalVariation.clear();
        int index = ThreadLocalRandom.current().nextInt(bestVariations.size());
        principalVariation.addAll(bestVariations.get(index));
        Move bestMove = bestVariations.get(index).get(0);

        updateTranspositionTable(game, value, bestMove, alphaOrig, beta, depth);

        xml.atDebug().log("<summary alpha=\"{}\" beta=\"{}\" score= \"{}\" total=\"{}\" pruned=\"{}\" best-move=\"{}\" history=\"{}\"/>", alpha, beta, value, moves.size(), moves.size() - evaluatedCount, bestMove.toLongSan(), principalVariation.stream().map(Move::toLongSan).collect(Collectors.joining(" ")));
        xml.atDebug().log("</node>");

        return value;
    }

    private void updateTranspositionTable(Game game, int value, Move best, int alphaOrig, int beta, int depth) {
        TranspositionTable.NodeType nt;
        if (value <= alphaOrig)
            nt = TranspositionTable.NodeType.UPPER_BOUND;
        else if (value >= beta)
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

