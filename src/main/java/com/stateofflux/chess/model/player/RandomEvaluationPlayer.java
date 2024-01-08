package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.PawnMoves;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RandomEvaluationPlayer extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomEvaluationPlayer.class);

    static final int DEFAULT_SEARCH_DEPTH = 2;
    protected Game game;
    boolean resultChanges = false;

    public RandomEvaluationPlayer(PlayerColor color) {
        this.color = color;
    }

    /*
      // Example picking best move in a chess game using negamax function above
      // https://en.wikipedia.org/wiki/Negamax

      function think(boardState) is
          allMoves := generateLegalMoves(boardState)
          bestMove := null
          bestEvaluation := -∞

          for each move in allMoves
              board.apply(move)
              evaluateMove := -negamax(boardState, depth=3)
              board.undo(move)
              if evaluateMove > bestEvaluation
                  bestMove := move
                  bestEvaluation := evaluateMove

          return bestMove
       */
    public Move getNextMove(Game game) {
        // LOGGER.info("Player ({}): {}", game.getActivePlayerColor(), game.getClock());

        this.game = game;

        MoveList<Move> moves = game.generateMoves();
        Move bestMove = null;
        int bestEvaluation = Integer.MIN_VALUE;

        for(Move move: moves) {
            game.move(move);
            int result = -negaMax(DEFAULT_SEARCH_DEPTH - 1);
            game.undo();

            if(result > bestEvaluation) {
                bestMove = move;
                bestEvaluation = result;
            }
        }

        // return the move with the largest value
        return bestMove;
    }

    /*
     * function negamax(node, depth, color) is
     *     if depth = 0 or node is a terminal node then
     *         return color × the heuristic value of node
     *     value := −∞
     *     for each child of node do
     *         value := max(value, −negamax(child, depth − 1, −color))
     *     return value
     */
    protected int negaMax(int depth) {
        if(depth == 0)
            return evaluate();

       int result = Integer.MIN_VALUE;
        MoveList<Move> moves = game.generateMoves();

        for(Move move: moves) {
            game.move(move);

            result = Math.max(result, -negaMax(depth - 1));

            game.undo();
        }

        return result;
    }

    public String toString() {
        return "RandomMovePlayer: " + color;
    }

    /*
     *   https://www.chessprogramming.org/Evaluation - symmetric evaluation function
     *
     *   f(p) = 200(K-K')
     *          + 9(Q-Q')
     *          + 5(R-R')
     *          + 3(B-B' + N-N')
     *          + 1(P-P')
     *          - 0.5(D-D' + S-S' + I-I')
     *          + 0.1(M-M') + ...
     *
     *   KQRBNP = number of kings, queens, rooks, bishops, knights and pawns
     *   D,S,I = doubled, blocked and isolated pawns
     *   M = Mobility (the number of legal moves)
     *
     * Always from the white player's perspective
     */
    protected int evaluate() {
        Board b = game.getBoard();
        int mobility = 0;  // ignoring for the moment.
        long pawns = b.getWhitePawns();
        long otherPawns = b.getBlackPawns();

        int pawnEvaluation = (PawnMoves.pawnEvaluation(pawns, otherPawns, true) -
            PawnMoves.pawnEvaluation(otherPawns, pawns, false)) / 2;

/*
        if(pawnEvaluation != 0)
            LOGGER.info("pawnEval : {}", pawnEvaluation);
*/

        int result = 200 * (c(b.getWhiteKingBoard()) - c(b.getBlackKingBoard()))
            + 9 * (c(b.getWhiteQueenBoard()) - c(b.getBlackQueenBoard()))
            + 5 * (c(b.getWhiteRookBoard()) - c(b.getBlackRookBoard()))
            + 3 * (c(b.getWhiteBishopBoard()) - c(b.getBlackBishopBoard()))
            + 3 * (c(b.getWhiteKnightBoard()) - c(b.getBlackKnightBoard()))
            + c(b.getWhitePawns()) - c(b.getBlackPawns())
            - pawnEvaluation
            + mobility;

     return result;
    }

    protected int c(long l) {
        return Long.bitCount(l);
    }

}
