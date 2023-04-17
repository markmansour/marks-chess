package com.stateofflux.chess.model;

import java.util.BitSet;

public class Player {
    protected Board board;
    protected PlayerColor color;

    public Player(Board board, PlayerColor color) {
        this.board = board;
        this.color = color;
    }

    public String[] validMoves(String position) {
        // 1. get the piece at the poisition
        int location = Board.convertStringToIndex(position);
        char piece = getPieceAtLocation(location);

        // basics
        // 2. that piece has allowed moves (e.g. pawn 1-2 forward, rook horizontal/vertical, bishop diagonal, etc)
        // 3. all pieces except pawns can finish their move on top of another piece
        // 4. all pieces except pawn and knights need a clear run to their final position
        // advanced
        // 5. pawns take on the diagnal
        // very advanced
        // en passent
        // castling
        BitSet occupied = this.board.getOccupied();

        return null;

    }

    public void move(String from, String to) {
        board.move(from, to);
    }
}
