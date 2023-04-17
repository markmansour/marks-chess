package com.stateofflux.chess.model;

public class Game {
    protected Player white;
    protected Player black;
    protected Board board;

    public Game() {
        this.board = new Board();
        this.white = new HumanPlayer(this.board, PlayerColor.WHITE);
        this.black = new HumanPlayer(this.board, PlayerColor.BLACK);
    }
}
