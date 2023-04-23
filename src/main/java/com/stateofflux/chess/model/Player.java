package com.stateofflux.chess.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    protected Board board;
    protected PlayerColor color;

    public Player(Board board, PlayerColor color) {
        this.board = board;
        this.color = color;
    }

    /*
     * Map<Integer, Long>
     * Integer - source location
     * Long - a bitmap of possible destinations
     */
    public Map<Integer, Long> validMoves(String position) {
        Map<Integer, Long> results = new HashMap<>();
        long destination;

        // 1. get the piece at the poisition
        int location = Board.convertPositionToLocation(position);
        // int sign = (color == PlayerColor.WHITE) ? 1 : -1;
        Piece piece = this.board.getPieceAtLocation(location);

        // check if player is trying to move a valid piece
        if (this.color != piece.getColor())
            throw new IllegalArgumentException("You can't move your opponent's pieces!");

        // basics
        // 2. that piece has allowed moves (e.g. pawn 1-2 forward, rook
        // horizontal/vertical, bishop diagonal, etc)
        switch (piece) {
            case WHITE_PAWN:
                // https://learning-oreilly-com.ezproxy.spl.org/library/view/effective-java-3rd/9780134686097/ch2.xhtml#lev3
                // BoardMoves moves = new BoardMoves.Builder(this.board, location)
                //                      .moving(Direction.UP)
                //                      .max(2)
                //                      .onlyIfEmpty()
                //                      .build();

                // // BoardMoves pawnMoves = new BoardMoves.Builder(this.board, location)
                // //                              .pawnNonCaptureMoves(Piece.WHITE_PAWN));

                // // keep the board and starting loction
                // moves.add(Direction.UP_LEFT, Direction.UP_RIGHT)
                //      .max(1)
                //      .onlyIfOpponent();

//                results.put(Integer.valueOf(location), moves.getAllMoves());
                // moves.getNonCaptureMoves()
                // moves.getCaptureMoves();



                // the pawn can move 1 or 2 spaces forward if the space is empty

                // destination = (1L << (location + Direction.UP.getDistance()) |
                //               1L << (location + (Direction.UP.getDistance() * 2))) &
                //               emptyBoard;

                // // the pawn can take on the diagnal
                // destination |= usedBoard
                // // can take on the diagnal
                // destination = location + Direction.UP_LEFT.getDistance();
                // if (this.board.hasBlackPiece(destination))
                //     destinations.add(destination);

                // destination = location + Direction.UP_RIGHT.getDistance();
                // if (this.board.hasBlackPiece(destination))
                //     destinations.add(destination);

                // results.put(Integer.valueOf(location), destinations.toArray(new Integer[destinations.size()]));

                break;

            // Knigt moves in an L shape (2 x 1)
            case WHITE_KNIGHT:
                break;

            // Rook moves horizontal or vertical as many squares as open
            case WHITE_ROOK:
                break;

            // Bishop moves diagonally as many squares as open
            case WHITE_BISHOP:
                break;

            // King moves one square in any direction
            case WHITE_KING:
                break;

            // Queen moves horizontally, vertically or diagonally in as many squares as are
            // open.
            case WHITE_QUEEN:
                break;

            // this should never happen!!!!
            default:
                break;
        }

        // 3. all pieces except pawns can finish their move on top of another piece
        // 4. all pieces except pawn and knights need a clear run to their final
        // position
        // advanced
        // 5. pawns take on the diagnal
        // very advanced
        // en passent
        // castling
        // BitSet occupied = this.board.getOccupied();

        return results;
    }

    public void move(String from, String to) {
        board.move(from, to);
    }
}
