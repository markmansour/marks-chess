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
        int sign = (color == PlayerColor.WHITE) ? 1 : -1;
        char piece = this.board.getPieceAtLocation(location);

        // check if player is trying to move a valid piece
        if(Character.isUpperCase(piece) && color == PlayerColor.BLACK ||
            Character.isLowerCase(piece) && color == PlayerColor.WHITE) {
                return new String[] { "You can't move your opponent's pieces!" };
        }

        // basics
        // 2. that piece has allowed moves (e.g. pawn 1-2 forward, rook horizontal/vertical, bishop diagonal, etc)
        switch(Character.toLowerCase(piece)) {
            case 'p':
                // can move "foward", 1 or 2 spaces

                // can take on the diagnal
                break;

            // Knigt moves in an L shape (2 x 1)
            case 'n':
            break;

            // Rook moves horizontal or vertical as many squares as open
            case 'r':
            break;

            // Bishop moves diagonally as many squares as open
            case 'b':
            break;

            // King moves one square in any direction
            case 'k':

                break;

            // Queen moves horizontally, vertically or diagonally in as many squares as are open.
            case 'q':
            break;

            // this should never happen!!!!
            default:
            break;
        }

        // 3. all pieces except pawns can finish their move on top of another piece
        // 4. all pieces except pawn and knights need a clear run to their final position
        // advanced
        // 5. pawns take on the diagnal
        // very advanced
        // en passent
        // castling
        // BitSet occupied = this.board.getOccupied();

        return null;
    }

    public void move(String from, String to) {
        board.move(from, to);
    }
}
