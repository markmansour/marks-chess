package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.PawnMoves;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static java.lang.Long.bitCount;

/*
 * Example picking best move in a chess game using negamax function above
 * https://en.wikipedia.org/wiki/Negamax
*/
public class BasicNegaMaxPlayer extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNegaMaxPlayer.class);

    protected static final int DEFAULT_SEARCH_DEPTH = 2;
    protected static final int KING_VALUE = 20000;
    protected static final int KNIGHT_VALUE = 320;
    protected static final int BISHOP_VALUE = 330;
    protected static final int ROOK_VALUE = 500;
    protected static final int QUEEN_VALUE = 900;
    protected static final int PAWN_VALUE = 100;

    protected Game game;

    public BasicNegaMaxPlayer(PlayerColor color) {
        this.color = color;
    }

    /*
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
        List<Move> bestMoves = new ArrayList<Move>();

        for(Move move: moves) {
            game.move(move);
            int result = -negaMax(DEFAULT_SEARCH_DEPTH - 1);
            game.undo();

            if(result == bestEvaluation) {
                bestMoves.add(move);
            } else if(result > bestEvaluation) {
                bestMoves.clear();
                bestMoves.add(move);
                bestMove = move;
                bestEvaluation = result;
            }
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        Move m = bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));

        return m;
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
        MoveList<Move> moves = game.pseudoLegalMovesFor();

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

        return
            KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
            + QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
            + ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
            + BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
            + KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
            + PAWN_VALUE * (bitCount(b.getWhitePawns()) - bitCount(b.getBlackPawns()))
            - pawnEvaluation
            + mobility;
    }
}
