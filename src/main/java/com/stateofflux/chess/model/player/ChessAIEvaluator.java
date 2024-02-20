package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.pieces.Piece;

import java.util.Map;

import static java.lang.Long.bitCount;

/*
 * Implementation of https://github.com/zeyu2001/chess-ai/blob/main/js/main.js#L132 (evaluateBoard)
 */
public class ChessAIEvaluator extends PieceSquareEvaluator {
    protected static final Map<Character, Integer> PIECE_WEIGHTS = Map.of(
        Piece.PAWN_ALGEBRAIC, 100,
        Piece.KNIGHT_ALGEBRAIC, 280,
        Piece.BISHOP_ALGEBRAIC, 320,
        Piece.ROOK_ALGEBRAIC, 479,
        Piece.QUEEN_ALGEBRAIC, 929,
        Piece.KING_ALGEBRAIC, 60000
    );

    private static final int[] PAWN_TABLE = {
        100, 100, 100, 100, 105, 100, 100, 100,
        78, 83, 86, 73, 102, 82, 85, 90,
        7, 29, 21, 44, 40, 31, 44, 7,
        -17, 16, -2, 15, 14, 0, 15, -13,
        -26, 3, 10, 9, 6, 1, 0, -23,
        -22, 9, 5, -11, -10, -2, 3, -19,
        -31, 8, -7, -37, -36, -14, 3, -31,
        0, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] KNIGHT_TABLE = {
        -66, -53, -75, -75, -10, -55, -58, -70,
        -3, -6, 100, -36, 4, 62, -4, -14,
        10, 67, 1, 74, 73, 27, 62, -2,
        24, 24, 45, 37, 33, 41, 25, 17,
        -1, 5, 31, 21, 22, 35, 2, 0,
        -18, 10, 13, 22, 18, 15, 11, -14,
        -23, -15, 2, 0, 2, 0, -23, -20,
        -74, -23, -26, -24, -19, -35, -22, -69,
    };

    private static final int[] BISHOP_TABLE = {
        -59, -78, -82, -76, -23, -107, -37, -50,
        -11, 20, 35, -42, -39, 31, 2, -22,
        -9, 39, -32, 41, 52, -10, 28, -14,
        25, 17, 20, 34, 26, 25, 15, 10,
        13, 10, 17, 23, 17, 16, 0, 7,
        14, 25, 24, 15, 8, 25, 20, 15,
        19, 20, 11, 6, 7, 6, 20, 16,
        -7, 2, -15, -12, -14, -15, -10, -10,
    };

    private static final int[] ROOK_TABLE = {
        35, 29, 33, 4, 37, 33, 56, 50,
        55, 29, 56, 67, 55, 62, 34, 60,
        19, 35, 28, 33, 45, 27, 25, 15,
        0, 5, 16, 13, 18, -4, -9, -6,
        -28, -35, -16, -21, -13, -29, -46, -30,
        -42, -28, -42, -25, -25, -35, -26, -46,
        -53, -38, -31, -26, -29, -43, -44, -53,
        -30, -24, -18, 5, -2, -18, -31, -32,
    };

    private static final int[] QUEEN_TABLE = {
        4, 54, 47, -99, -99, 60, 83, -62,
        -32, 10, 55, 56, 56, 55, 10, 3,
        -62, 12, -57, 44, -67, 28, 37, -31,
        -55, 50, 11, -4, -19, 13, 0, -49,
        -55, -43, -52, -28, -51, -47, -8, -50,
        -47, -42, -43, -79, -64, -32, -29, -32,
        -4, 3, -14, -50, -57, -18, 13, 4,
        17, 30, -3, -14, 6, -1, 40, 18,
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
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50,
    };

    public ChessAIEvaluator() {
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

    // view from white's perspective
    @Override
    public int evaluate(Game game, int depth) {
        int bonus = 0;
        int sideMoved = game.getActivePlayerColor().isBlack() ? 1 : -1;

        if(game.isCheckmated()) {
            return (MATE_VALUE - depth) * sideMoved;  // prioritize mate values that take fewer moves.
        }

        if(game.isDraw() || game.isRepetition() || game.isStalemate())
            return 0;

        if(game.isChecked()) {
            bonus += 50 * sideMoved;
        }

        Board b = game.getBoard();

        int materialScore =
            PIECE_WEIGHTS.get(Piece.KING_ALGEBRAIC) * (bitCount(b.getWhiteKingBoard()) - bitCount(b.getBlackKingBoard()))
                + PIECE_WEIGHTS.get(Piece.QUEEN_ALGEBRAIC) * (bitCount(b.getWhiteQueenBoard()) - bitCount(b.getBlackQueenBoard()))
                + PIECE_WEIGHTS.get(Piece.ROOK_ALGEBRAIC) * (bitCount(b.getWhiteRookBoard()) - bitCount(b.getBlackRookBoard()))
                + PIECE_WEIGHTS.get(Piece.BISHOP_ALGEBRAIC) * (bitCount(b.getWhiteBishopBoard()) - bitCount(b.getBlackBishopBoard()))
                + PIECE_WEIGHTS.get(Piece.KNIGHT_ALGEBRAIC) * (bitCount(b.getWhiteKnightBoard()) - bitCount(b.getBlackKnightBoard()))
                + PIECE_WEIGHTS.get(Piece.PAWN_ALGEBRAIC) * (bitCount(b.getWhitePawnBoard()) - bitCount(b.getBlackPawnBoard()));

        Move lastMove = game.getLastMove();

        if(lastMove.isCapture()) {
            bonus += PIECE_WEIGHTS.get(lastMove.getCapturePiece().getAlgebraicChar()) * sideMoved;
        }

        // this does the same as promotion math and general square math as I'm calculating the
        // score from scratch, whereas chessai does an incremental update.
        int boardScore = boardScore(game);

        // return bonus + materialScore + boardScore;
        return bonus + materialScore + (boardScore * sideMoved);
    }

    protected void checkForEndGame(Game game) {
        if (endGame) return;

        if (bitCount(game.getBoard().getBlack()) < 4 || bitCount(game.getBoard().getWhite()) < 4) {
            this.endGame = true;
            pieceSquareTables[Piece.WHITE_KING.getIndex()] = PieceSquareEvaluator.visualToArrayLayout(KING_ENDGAME_TABLE);
            pieceSquareTables[Piece.BLACK_KING.getIndex()] = PieceSquareEvaluator.visualToArrayLayout(PieceSquareEvaluator.transposeWhiteToBlack(KING_ENDGAME_TABLE));
        }
    }
}
