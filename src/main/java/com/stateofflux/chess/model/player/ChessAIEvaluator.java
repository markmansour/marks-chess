package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;

import java.util.Map;

import static java.lang.Long.bitCount;

public class ChessAIEvaluator {
    protected static Map<Character, Integer> PIECE_WEIGHTS = Map.of(
        Piece.PAWN_ALGEBRAIC, 100,
        Piece.KNIGHT_ALGEBRAIC, 280,
        Piece.BISHOP_ALGEBRAIC, 320,
        Piece.ROOK_ALGEBRAIC, 479,
        Piece.QUEEN_ALGEBRAIC, 929,
        Piece.KING_ALGEBRAIC, 60000
    );

    protected static final int[] PAWN_TABLE = {
        100, 100, 100, 100, 105, 100, 100, 100,
        78, 83, 86, 73, 102, 82, 85, 90,
        7, 29, 21, 44, 40, 31, 44, 7,
        -17, 16, -2, 15, 14, 0, 15, -13,
        -26, 3, 10, 9, 6, 1, 0, -23,
        -22, 9, 5, -11, -10, -2, 3, -19,
        -31, 8, -7, -37, -36, -14, 3, -31,
        0, 0, 0, 0, 0, 0, 0, 0,
    };

    protected static final int[] KNIGHT_TABLE = {
        -66, -53, -75, -75, -10, -55, -58, -70,
        -3, -6, 100, -36, 4, 62, -4, -14,
        10, 67, 1, 74, 73, 27, 62, -2,
        24, 24, 45, 37, 33, 41, 25, 17,
        -1, 5, 31, 21, 22, 35, 2, 0,
        -18, 10, 13, 22, 18, 15, 11, -14,
        -23, -15, 2, 0, 2, 0, -23, -20,
        -74, -23, -26, -24, -19, -35, -22, -69,
    };

    protected static final int[] BISHOP_TABLE = {
        -59, -78, -82, -76, -23, -107, -37, -50,
        -11, 20, 35, -42, -39, 31, 2, -22,
        -9, 39, -32, 41, 52, -10, 28, -14,
        25, 17, 20, 34, 26, 25, 15, 10,
        13, 10, 17, 23, 17, 16, 0, 7,
        14, 25, 24, 15, 8, 25, 20, 15,
        19, 20, 11, 6, 7, 6, 20, 16,
        -7, 2, -15, -12, -14, -15, -10, -10,
    };

    protected static final int[] ROOK_TABLE = {
        35, 29, 33, 4, 37, 33, 56, 50,
        55, 29, 56, 67, 55, 62, 34, 60,
        19, 35, 28, 33, 45, 27, 25, 15,
        0, 5, 16, 13, 18, -4, -9, -6,
        -28, -35, -16, -21, -13, -29, -46, -30,
        -42, -28, -42, -25, -25, -35, -26, -46,
        -53, -38, -31, -26, -29, -43, -44, -53,
        -30, -24, -18, 5, -2, -18, -31, -32,
    };

    protected static final int[] QUEEN_TABLE = {
        4, 54, 47, -99, -99, 60, 83, -62,
        -32, 10, 55, 56, 56, 55, 10, 3,
        -62, 12, -57, 44, -67, 28, 37, -31,
        -55, 50, 11, -4, -19, 13, 0, -49,
        -55, -43, -52, -28, -51, -47, -8, -50,
        -47, -42, -43, -79, -64, -32, -29, -32,
        -4, 3, -14, -50, -57, -18, 13, 4,
        17, 30, -3, -14, 6, -1, 40, 18,
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
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50,
    };

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

    protected static int[] transposeWhiteToBlack(int[] a) {
        int [] result = new int[a.length];  // 64!

        for(int i = 0; i < 8; i++) {
            System.arraycopy(a, i * 8, result, 56 - (i * 8), 8);
        }

        return result;
    }


    protected int nodesEvaluated = 0;
    protected boolean endGame = false;

    protected int[][] PieceSquareTables = new int[12][];

    protected void initializePSTs() {
        PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(KING_MIDGAME_TABLE));
        PieceSquareTables[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        PieceSquareTables[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(transposeWhiteToBlack(QUEEN_TABLE));
        PieceSquareTables[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        PieceSquareTables[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(BISHOP_TABLE));
        PieceSquareTables[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        PieceSquareTables[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(KNIGHT_TABLE));
        PieceSquareTables[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        PieceSquareTables[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(ROOK_TABLE));
        PieceSquareTables[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        PieceSquareTables[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(PAWN_TABLE));
    }

    public int evaluate(Game game, int depth, PlayerColor pc) {
        int bonus = 0;
        int sideMultiplier = getSideToMove(pc);

        if(game.isCheckmated()) {
            return getSideToMove(pc) == -1 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        if(game.isDraw() || game.isRepetition() || game.isStalemate())
            return 0;

        if(game.isChecked()) {
            bonus += 50 * sideMultiplier;
        }

/*
        if(currentMove != null && currentMove.isCapture()) {
            bonus += PIECE_WEIGHTS.get(destinationPiece.getAlgebraicChar()) +
                PieceSquareTables[destinationPiece.getIndex()][currentMove.getTo()] * sideMultiplier;
        }

*/
        int boardScore = boardScore(game);

        // return (materialScore + mobilityScore + bonus + boardScore(game)) * sideToMove;
        bonus += boardScore;

        return bonus * sideMultiplier;
    }

    protected int getSideToMove(PlayerColor pc) {
        return pc.isWhite() ? 1 : -1;
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

    private void checkForEndGame(Game game) {
        if (endGame) return;

        if(bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
            PieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_ENDGAME_TABLE);
            PieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(KING_ENDGAME_TABLE));
        }
    }

    private boolean isEndGame() {
        return this.endGame;
    }

    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

}
