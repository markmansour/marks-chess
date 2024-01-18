package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Long.bitCount;

/*
 * Example picking best move in a chess game using negamax function above
 * https://en.wikipedia.org/wiki/Negamax
*/
public class BasicNegaMaxPlayer extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNegaMaxPlayer.class);

    protected static final int KING_VALUE = 20000;
    protected static final int KNIGHT_VALUE = 320;
    protected static final int BISHOP_VALUE = 330;
    protected static final int ROOK_VALUE = 500;
    protected static final int QUEEN_VALUE = 900;
    protected static final int PAWN_VALUE = 100;

    protected Game game;

    /*
     Tables from: https://www.chessprogramming.org/Simplified_Evaluation_Function
     But!  The tables listed are in the wrong order (really!?) as they are in visual order rather than array order.
     e.g.  Bottom left represents index 0, but if copied straight into an array would be index 54.  Therefore reverse
     the line order so the bottom line (bottom 8 values) becomes the top line (the first 8 values).
     */
    protected static final int[] PAWN_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    protected static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50,
    };

    protected static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20,
    };

    protected static final int[] ROOK_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };

    protected static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    protected static final int[] KING_MIDGAME_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };

    /*
     * Additionally we should define where the ending begins. For me it might be either if:
     * - Both sides have no queens or
     * - Every side which has a queen has additionally no other pieces or one minorpiece maximum.
     */
    protected static final int[] KING_ENDGAME_TABLE = {
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };

    protected static final int[][] LOOKUP_TABLES = new int[14][];

    static {
        LOOKUP_TABLES[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        LOOKUP_TABLES[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(reverse(KING_MIDGAME_TABLE));
        LOOKUP_TABLES[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        LOOKUP_TABLES[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(reverse(QUEEN_TABLE));
        LOOKUP_TABLES[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        LOOKUP_TABLES[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(reverse(BISHOP_TABLE));
        LOOKUP_TABLES[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        LOOKUP_TABLES[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(reverse(KNIGHT_TABLE));
        LOOKUP_TABLES[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        LOOKUP_TABLES[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(reverse(ROOK_TABLE));
        LOOKUP_TABLES[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        LOOKUP_TABLES[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(reverse(PAWN_TABLE));
        LOOKUP_TABLES[12]                            = visualToArrayLayout(KING_ENDGAME_TABLE);
        LOOKUP_TABLES[13]                            = visualToArrayLayout(reverse(KING_ENDGAME_TABLE));
    }

    private boolean endGame = false;

    /*
     * Assumes size of 64.
     */
    private static int[] visualToArrayLayout(int[] visualLayout) {
        assert visualLayout.length == 64;

        int[] arrayLayout = new int[visualLayout.length];
        for(int i = 7; i >= 0; i--) {
            System.arraycopy(
                visualLayout, i * 8,
                arrayLayout, (7-i)*8,
                8);
        }

        return arrayLayout;
    }

    private static int[] reverse(int[] a) {
        return IntStream.rangeClosed(1, a.length).map(i -> a[a.length - i]).toArray();
    }

    public BasicNegaMaxPlayer(PlayerColor color) {
        this.color = color;
        this.searchDepth = DEFAULT_SEARCH_DEPTH;
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
        List<Move> bestMoves = new ArrayList<>();

        for(Move move: moves) {
            game.move(move);
            int score = -negaMax(searchDepth - 1);
            game.undo();

            // LOGGER.info("Move ({}): {}", move, score);

            if(score == bestEvaluation) {
                bestMoves.add(move);
            } else if(score > bestEvaluation) {
                bestMoves.clear();
                bestMoves.add(move);
                bestEvaluation = score;
            }
        }

        if(!isEndGame()) {
            checkForEndGame();
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.

        return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
    }

    private void checkForEndGame() {
        if (endGame) return;

        if(bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
        }
    }

    private boolean isEndGame() {
        return this.endGame;
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
        if(depth == 0)  // can the king count be 0 here?  Should we be checking?
            return evaluate() * (game.getActivePlayerColor().isWhite() ? 1 : -1);

        int result = Integer.MIN_VALUE;
        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty()) {
            result = evaluate() * (game.getActivePlayerColor().isWhite() ? 1 : -1);

            if(!game.isChecked())
                result += (game.getActivePlayerColor().isWhite() ? -1 : +1);  // stalemates worth less than checkmate
        }

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

/*
        long pawns = b.getWhitePawns();
        long otherPawns = b.getBlackPawns();

        int pawnEvaluation = (PawnMoves.pawnEvaluation(pawns, otherPawns, true) -
            PawnMoves.pawnEvaluation(otherPawns, pawns, false)) / 2;
*/

        if(bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()) == 1)
            LOGGER.info("attempting to take a king");

        int boardScore = boardScore();

        int score =
            KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
            + QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
            + ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
            + BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
            + KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
            + PAWN_VALUE * (bitCount(b.getWhitePawns()) - bitCount(b.getBlackPawns()))
//            - pawnEvaluation
            + boardScore
            + mobility;

        if(game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            score += 500;
        }

        if(game.isRepetition()) {
            score -= 500;
        }

        return score;
    }

    protected int boardScore() {
        int score = 0;
        Board b = game.getBoard();

        // LOGGER.info(game.asFen());

        for(int i = 0; i < 64; i++) {
            Piece p = b.get(i);
            if(p.isEmpty()) continue;

//            LOGGER.info("square {} = {}", i, LOOKUP_TABLES[p.getIndex()][i]);
            try {
                if(isEndGame() && (p == Piece.WHITE_KING) && bitCount(game.getBoard().getWhite()) < 4)
                    score += LOOKUP_TABLES[11][i];
                else if(isEndGame() && (p == Piece.BLACK_KING) && bitCount(game.getBoard().getBlack()) < 4)
                    score += LOOKUP_TABLES[12][i] * -1;
                else
                    score += LOOKUP_TABLES[p.getIndex()][i] * ((p.getColor().isWhite() ? 1 : -1));
            } catch(ArrayIndexOutOfBoundsException e) {
                LOGGER.info("pause");
            }
        }

        return score;
    }
}
