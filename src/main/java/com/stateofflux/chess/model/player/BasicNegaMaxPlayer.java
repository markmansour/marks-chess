package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

import static com.stateofflux.chess.model.pieces.KingMoves.MATE_VALUE;
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

    Deque evaluatingMoves;

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
    private int bestMoveScore = 0;

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

    public BasicNegaMaxPlayer(PlayerColor pc) {
        super(pc);
        this.searchDepth = DEFAULT_SEARCH_DEPTH;
        evaluatingMoves = new ArrayDeque<Move>();
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();

        int max = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        evaluatingMoves.clear();

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            int score = negaMax(game, searchDepth);
            game.undo();
            evaluatingMoves.pollLast();
            // LOGGER.info("{} - move ({}): {}", game.getActivePlayer(), move, score);

            if(score == max) {
                bestMoves.add(move);
            } else if(score > max) {
                bestMoves.clear();
                bestMoves.add(move);
                max = score;
            }
        }

        assert !bestMoves.isEmpty();

        // There are many values with the same score so randomly pick a value.  By randomly picking a value
        // we don't continue to pick the "first" result.
        Move bestMove = bestMoves.get(rand.nextInt(bestMoves.size()));
        this.bestMoveScore = max;

        // LOGGER.info("{}: {}: best move is {}, score {}", searchDepth, game.asFen(), bestMove, max);

        return bestMove;
    }

    /*
     * https://www.chessprogramming.org/Negamax
     *
     *   int negaMax( int depth ) {
     *       if ( depth == 0 ) return evaluate();
     *       int max = -oo;
     *       for ( all moves)  {
     *           score = -negaMax( depth - 1 );
     *           if( score > max )
     *               max = score;
     *       }
     *       return max;
     *   }
     */
    protected int negaMax(Game game, int depth) {
        if(depth == 0)  // can the king count be 0 here?  Should we be checking?
            return evaluate(game);

        int max = Integer.MIN_VALUE;
        int score;
        Move bestMove = null; // for debugging.

        MoveList<Move> moves = game.generateMoves();

        // node is terminal
        if(moves.isEmpty())
            return evaluate(game);

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            score = -negaMax(game,depth - 1);
            game.undo();
            evaluatingMoves.pollLast();

            if(score > max) {
                max = score;
                bestMove = move;
            }
        }

        // LOGGER.info("negamax bestmove at depth {}: {}", depth, bestMove);
        return max;
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
    public int evaluate(Game game) {
        Board b = game.getBoard();
        int bonus = 0;

        // from the perspective of the white player
        int materialScore =
            KING_VALUE * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + QUEEN_VALUE * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + ROOK_VALUE * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + BISHOP_VALUE * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + KNIGHT_VALUE * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + PAWN_VALUE * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

        int mobilityWeight = 1;

        MoveList<Move> whiteMoves = game.generateMovesFor(PlayerColor.WHITE);
        MoveList<Move> blackMoves = game.generateMovesFor(PlayerColor.BLACK);

        // from the perspective of the white player
        int mobilityScore = mobilityWeight *
            (whiteMoves.size() - blackMoves.size());

        // sideToMove is the value of the move just completed.  The game counter has already moved
        // on so we need to reverse the player color.  Therefore if the game thinks it is white's turn
        // then it was black that just moved.
        int sideToMove = getSideToMove(game);

        // if we have checkmate, then this is the best move so return immediately!
        // isCheckmated is really expensive!
        if(game.isCheckmated()) {
            // LOGGER.info("**************** CHECKMATED: {}", evaluatingMoves);
            int mateValue = MATE_VALUE - evaluatingMoves.size();  // prioritize mate values that take fewer moves.
            return game.getActivePlayerColor().isWhite() ? -mateValue : mateValue;
/*
        } else if(game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            // LOGGER.info("**************** CHECK: {}", evaluatingMoves);
            bonus += (500 * sideToMove);
*/
        }

        int value =  (materialScore + mobilityScore + bonus) * sideToMove;
        value += boardScore(game);  // boardScore already takes side into account

        return value;
    }

    protected int getSideToMove(Game game) {
        return game.getActivePlayerColor().isWhite() ? -1 : 1;
    }

    protected int boardScore(Game game) {
        int score = 0;
        Board b = game.getBoard();

        // LOGGER.info(game.asFen());

        for(int i = 0; i < 64; i++) {
            Piece p = b.get(i);
            if(p.isEmpty()) continue;

            score += LOOKUP_TABLES[p.getIndex()][i];

            /*
//            LOGGER.info("square {} = {}", i, LOOKUP_TABLES[p.getIndex()][i]);
            try {
                if(isEndGame() && (p == Piece.WHITE_KING) && bitCount(game.getBoard().getWhite()) < 4)
                    score += LOOKUP_TABLES[11][i];
                else if(isEndGame() && (p == Piece.BLACK_KING) && bitCount(game.getBoard().getBlack()) < 4)
                    score += LOOKUP_TABLES[12][i] * -1;
                else
                    score += LOOKUP_TABLES[p.getIndex()][i];
//                    score += LOOKUP_TABLES[p.getIndex()][i] * ((p.getColor().isWhite() ? 1 : -1));
            } catch(ArrayIndexOutOfBoundsException e) {
                LOGGER.info("pause");
            }
        */
        }

        return score;
    }

    public int getBestMoveScore() {
        return bestMoveScore;
    }

    private void checkForEndGame(Game game) {
        if (endGame) return;

        if(bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
        }
    }

    private boolean isEndGame() {
        return this.endGame;
    }

    public Deque getEvaluatingMoves() {
        return evaluatingMoves;
    }
}
