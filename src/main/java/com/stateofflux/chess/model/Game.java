package com.stateofflux.chess.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stateofflux.chess.model.pieces.Piece;
import com.stateofflux.chess.model.pieces.PieceMoves;

public class Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;
    protected char[] castlingRights;
    protected char[] enPassantTarget;
    protected int halfmoveClock;
    protected int fullmoveCounter;
    protected Map<String, PieceMoves> nextMoves;
    protected int nextMovesCount;

    // game with players - intended for play
    public Game(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.board = new Board();
    }

    // game with no players - used for analysis
    public Game() {
        this.board = new Board();
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setCastlingRights(new char[] { 'K', 'Q', 'k', 'q' });
        this.setEnPassantTarget(new char[] { '-' });
        this.setHalfmoveClock(0);
        this.setFullmoveCounter(1);
    }

    // game that can start midway through - used for analysis
    public Game(String fenString) {
        FenString fen = new FenString(fenString);
        this.board = new Board(fen.getPiecePlacement());
        this.setActivePlayerColor(fen.getActivePlayerColor());
        this.setCastlingRights(fen.getCastlingRights());
        this.setEnPassantTarget(fen.getEnPassantTarget());
        this.setHalfmoveClock(fen.getHalfmoveClock());
        this.setFullmoveCounter(fen.getFullmoveCounter());
    }

    public String getPiecePlacement() {
        return this.board.toFenPiecePlacementString();
    }

    private void setFullmoveCounter(int fullmoveCounter) {
        this.fullmoveCounter = fullmoveCounter;
    }

    public int getFullmoveCounter() {
        return this.fullmoveCounter;
    }

    private void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    public int getHalfmoveClock() {
        return this.halfmoveClock;
    }

    private void setEnPassantTarget(char[] enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    public char[] getEnPassantTarget() {
        return this.enPassantTarget;
    }

    private void setCastlingRights(char[] castlingRights) {
        this.castlingRights = castlingRights;
    }

    public char[] getCastlingRights() {
        return this.castlingRights;
    }

    private void setActivePlayerColor(PlayerColor activePlayerColor) {
        this.activePlayerColor = activePlayerColor;
    }

    public PlayerColor getActivePlayerColor() {
        return this.activePlayerColor;
    }

    // iterate over all pieces on the board
    // for each piece, generate the moves for that piece
    // store the moves in a list
    // return number of moves
    // 1 ply
    public int generateMoves() {
        this.nextMoves = new HashMap<>();
        this.nextMovesCount = 0;

        // TODO find a faster way to iterate over the board.
        for (int i = 0; i < 64; i++) {
            Piece piece = this.board.getPieceAtLocation(i);

            if (piece == Piece.EMPTY || piece.getColor() != this.activePlayerColor)
                continue;

            PieceMoves bm = piece.generateMoves(this.board, i);
            if (bm.getMovesCount() > 0) {
                this.nextMoves.put(FenString.locationToAlgebraString(i), bm);
                LOGGER.info("Generated {} moves for {} at {}", bm.getMovesCount(), piece, i);
                this.nextMovesCount += bm.getMovesCount();
            }
        }

        return this.nextMovesCount;
    }

    public Map<String, PieceMoves> getNextMoves() {
        return this.nextMoves;
    }
}
