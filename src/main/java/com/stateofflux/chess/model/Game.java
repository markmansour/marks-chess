package com.stateofflux.chess.model;

import java.util.Scanner;
import java.util.function.IntPredicate;

public class Game {
    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;
    protected char[] castlingRights;
    protected char[] enPassantTarget;
    protected int halfmoveClock;
    protected int fullmoveCounter;

    // game with players - intended for play
    public Game(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.board = new Board();
    }

    // game with no players - used for analysis
    public Game() {
        this.board = new Board();
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
}
