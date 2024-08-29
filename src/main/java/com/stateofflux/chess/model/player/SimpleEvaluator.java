package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.pieces.Piece;

import static java.lang.Long.bitCount;

public class SimpleEvaluator extends PieceSquareEvaluator {
    /*
     Tables from: https://www.chessprogramming.org/Simplified_Evaluation_Function
     But!  The tables listed are in the wrong order (really!?) as they are in visual order rather than array order.
     e.g.  Bottom left represents index 0, but if copied straight into an array would be index 54.  Therefore, reverse
     the line order so the bottom line (bottom 8 values) becomes the top line (the first 8 values).
    */
    private static final int[] PAWN_TABLE = {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50,
    };

    private static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20,
    };

    private static final int[] ROOK_TABLE = {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_MIDGAME_TABLE = {
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
    private static final int[] KING_ENDGAME_TABLE = {
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };

    public SimpleEvaluator() {
        super();
        initializePSTs();
    }

    protected void initializePSTs() {
        pieceSquareTables[Piece.WHITE_KING.getIndex()]   = visualToArrayLayout(KING_MIDGAME_TABLE);
        pieceSquareTables[Piece.BLACK_KING.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(KING_MIDGAME_TABLE));
        pieceSquareTables[Piece.WHITE_QUEEN.getIndex()]  = visualToArrayLayout(QUEEN_TABLE);
        pieceSquareTables[Piece.BLACK_QUEEN.getIndex()]  = visualToArrayLayout(transposeWhiteToBlack(QUEEN_TABLE));
        pieceSquareTables[Piece.WHITE_BISHOP.getIndex()] = visualToArrayLayout(BISHOP_TABLE);
        pieceSquareTables[Piece.BLACK_BISHOP.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(BISHOP_TABLE));
        pieceSquareTables[Piece.WHITE_KNIGHT.getIndex()] = visualToArrayLayout(KNIGHT_TABLE);
        pieceSquareTables[Piece.BLACK_KNIGHT.getIndex()] = visualToArrayLayout(transposeWhiteToBlack(KNIGHT_TABLE));
        pieceSquareTables[Piece.WHITE_ROOK.getIndex()]   = visualToArrayLayout(ROOK_TABLE);
        pieceSquareTables[Piece.BLACK_ROOK.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(ROOK_TABLE));
        pieceSquareTables[Piece.WHITE_PAWN.getIndex()]   = visualToArrayLayout(PAWN_TABLE);
        pieceSquareTables[Piece.BLACK_PAWN.getIndex()]   = visualToArrayLayout(transposeWhiteToBlack(PAWN_TABLE));
    }

    protected void checkForEndGame(Game game) {
        if (endGame) return;

        if (bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
            pieceSquareTables[Piece.WHITE_KING.getIndex()] = PieceSquareEvaluator.visualToArrayLayout(KING_ENDGAME_TABLE);
            pieceSquareTables[Piece.BLACK_KING.getIndex()] = PieceSquareEvaluator.visualToArrayLayout(PieceSquareEvaluator.transposeWhiteToBlack(KING_ENDGAME_TABLE));
        }
    }

    @Override
    public String toString() {
        return "SimpleEvaluator";
    }
}
