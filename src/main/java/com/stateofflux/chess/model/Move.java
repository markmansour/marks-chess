package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.Piece;

public class Move {
    private final int from;
    private final int to;
    private final MoveFlag flags;
    private final Piece piece;

    public Move(Piece piece, int from, int to, MoveFlag flags) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.flags = flags;
    }

    public PlayerColor getColor() {
        return this.piece.getColor();
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public MoveFlag getFlags() {
        return flags;
    }

    public Piece getPiece() {
        return piece;
    }

    public String toSan() {
        return "TODO";
    }

    public String toLongSan() {
        return FenString.locationToSquare(from) +
            FenString.locationToSquare(to);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder()
            .append(piece.getPieceChar())
            .append(" : ")
            .append(toLongSan());
        return sb.toString();
    }
}
