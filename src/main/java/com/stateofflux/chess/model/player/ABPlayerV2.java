package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.stateofflux.chess.App.uci_logger;

public class ABPlayerV2 extends Player {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int MAX_DEPTH = 10; // Adjust this based on your requirements or time constraints

    public ABPlayerV2(PlayerColor color, Evaluator evaluator) {
        super(color, evaluator);
    }

    @Override
    public Move getNextMove(Game game) {
        List<Move> principalVariation = new ArrayList<>();

        Move result = depthSearch(game, getSearchDepth(), game.getActivePlayerColor().isWhite(), principalVariation);

        return result;
    }

    public Move depthSearch(Game state, int maxDepth, boolean maximizingPlayer, List<Move> principalVariation) {
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<Move> bestVariation = new ArrayList<>();

        for (int depth = 1; depth <= maxDepth; depth++) {
            nodesVisited = 0;
            List<Move> currentVariation = new ArrayList<>();
            int score = alphaBeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, maximizingPlayer, currentVariation);

            if ((maximizingPlayer && score > bestScore) || (!maximizingPlayer && score < bestScore)) {
                bestScore = score;
                bestVariation = new ArrayList<>(currentVariation);
            }

            logger.atInfo().log("info depth {} score {} nodes {} pv {}",
                depth,
                score,
                getNodesVisited(),
                currentVariation.stream().map(Move::toLongSan).collect(Collectors.joining(" "))
            );

            uci_logger.atInfo().log("info depth {} score {} nodes {} pv {}",
                depth,
                score,
                getNodesVisited(),
                currentVariation.stream().map(Move::toLongSan).collect(Collectors.joining(" "))
            );
        }

        principalVariation.clear();
        principalVariation.addAll(bestVariation);

        return principalVariation.get(0);
    }

    private int alphaBeta(Game game, int depth, int alpha, int beta, boolean maximizingPlayer, List<Move> principalVariation) {
        if (depth == 0)
            return evaluator.evaluate(game, depth);

        List<Move> moves = game.generateMoves();

        if (moves.isEmpty())
            return evaluator.evaluate(game, depth);

        List<Move> localVariation = new ArrayList<>();

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                List<Move> childVariation = new ArrayList<>();

                game.move(move);
                nodesVisited++;
                int eval = alphaBeta(game, depth - 1, alpha, beta, false, childVariation);
                game.undo();

                if (eval > maxEval) {
                    maxEval = eval;
                    localVariation.clear();
                    localVariation.add(move);
                    localVariation.addAll(childVariation);
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // beta cut-off
                }
            }
            principalVariation.clear();
            principalVariation.addAll(localVariation);
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                List<Move> childVariation = new ArrayList<>();

                game.move(move);
                nodesVisited++;
                int eval = alphaBeta(game, depth - 1, alpha, beta, true, childVariation);
                game.undo();

                if (eval < minEval) {
                    minEval = eval;
                    localVariation.clear();
                    localVariation.add(move);
                    localVariation.addAll(childVariation);
                }
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // alpha cut-off
                }
            }
            principalVariation.clear();
            principalVariation.addAll(localVariation);
            return minEval;
        }
    }
}
