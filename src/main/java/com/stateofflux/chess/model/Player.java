package com.stateofflux.chess.model;

public class Player {
    protected Board board;

    public Player(Board board) {
        this.board = board;
    }

    public void move(String from, String to) {
        board.move(from, to);
    }
}
