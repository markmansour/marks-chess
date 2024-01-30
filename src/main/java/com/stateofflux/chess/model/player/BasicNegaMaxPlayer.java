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

    protected static final int PAWN_VALUE = 100;
    protected static final int ROOK_VALUE = 500;
    protected static final int KNIGHT_VALUE = 320;
    protected static final int BISHOP_VALUE = 330;
    protected static final int QUEEN_VALUE = 900;
    protected static final int KING_VALUE = 20_000;

    protected Deque<Move> evaluatingMoves;
    protected int nodesEvaluated = 0;

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

    protected int[][] PieceSquareTables = new int[12][];

    protected void initializePSTs() {
        PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(reverse(KING_MIDGAME_TABLE));
        PieceSquareTables[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        PieceSquareTables[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(reverse(QUEEN_TABLE));
        PieceSquareTables[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        PieceSquareTables[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(reverse(BISHOP_TABLE));
        PieceSquareTables[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        PieceSquareTables[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(reverse(KNIGHT_TABLE));
        PieceSquareTables[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        PieceSquareTables[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(reverse(ROOK_TABLE));
        PieceSquareTables[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        PieceSquareTables[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(reverse(PAWN_TABLE));
    }

    protected boolean endGame = false;
    protected int bestMoveScore = 0;

    /*
     * Assumes size of 64.
     */
    protected static int[] visualToArrayLayout(int[] visualLayout) {
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

    protected static int[] reverse(int[] a) {
        return IntStream.rangeClosed(1, a.length).map(i -> a[a.length - i]).toArray();
    }

    public BasicNegaMaxPlayer(PlayerColor pc) {
        super(pc);
        this.searchDepth = DEFAULT_SEARCH_DEPTH;
        evaluatingMoves = new ArrayDeque<Move>();
        initializePSTs();
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        moves.sort(new MoveComparator());

        int max = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        evaluatingMoves.clear();

        for(Move move: moves) {
            evaluatingMoves.offerLast(move);
            game.move(move);
            int score = negaMax(game, searchDepth - 1);
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
     * https://www.chessprogramming.org/Evaluation - symmetric evaluation function
     *
     * While Minimax usually associates the white side with the max-player and black with the min-player and always
     * evaluates from the white point of view, NegaMax requires a symmetric evaluation in relation to
     * the side to move.
     *
     */
    public int evaluate(Game game) {
        nodesEvaluated++;

        Board b = game.getBoard();
        int bonus = 0;

        // sideToMove is the value of the move just completed.  The game counter has already moved
        // on so we need to reverse the player color.  Therefore if the game thinks it is white's turn
        // then it was black that just moved.
        int sideToMove = getSideToMove(game);

        if(game.isCheckmated()) {
            // LOGGER.info("**************** CHECKMATED: {}", evaluatingMoves);
            return (MATE_VALUE - evaluatingMoves.size()) * sideToMove;  // prioritize mate values that take fewer moves.
        }

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

        if(game.isChecked()) {
            // LOGGER.info("Game in check ({}): {}", score, game.asFen());
            // LOGGER.info("**************** CHECK: {}", evaluatingMoves);
            bonus += (500 * sideToMove);  // from white's perspective
        }

        // if attacking a space next to the king give a bonus

/*
        long kingAttacks = KingMoves.surroundingMoves(b.getKingLocation(getColor()));
        for(int dest: Board.bitboardToArray(kingAttacks)) {
            if(b.locationUnderAttack(getColor(), dest)) {
                bonus += (100 * sideToMove);
            }
        }
*/

        /*
         * In order for NegaMax to work, it is important to return the score relative to the side being evaluated.
         */
        return (materialScore + mobilityScore + bonus + boardScore(game)) * sideToMove;
    }

    protected int getSideToMove(Game game) {
        return getColor().isWhite() ? 1 : -1;
    }

    protected int boardScore(Game game) {
        int score = 0;
        Board b = game.getBoard();
        checkForEndGame(game);

        // LOGGER.info(game.asFen());

        for(int i = 0; i < 64; i++) {
            Piece p = b.get(i);
            if(p.isEmpty()) continue;

            score += PieceSquareTables[p.getIndex()][i] * (p.isWhite() ? 1 : -1);
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
            LOGGER.info("Using King Endgame Tables");
            PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_ENDGAME_TABLE);
            PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(reverse(KING_ENDGAME_TABLE));
        }
    }

    private boolean isEndGame() {
        return this.endGame;
    }

    public Deque<Move> getEvaluatingMoves() {
        return evaluatingMoves;
    }

    public int getNodesEvaluated() {
        return nodesEvaluated;
    }
}
