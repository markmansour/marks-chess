package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.PawnMoves;
import com.stateofflux.chess.model.pieces.Piece;

public class Move {
    public static final boolean CAPTURE = true;
    public static final boolean NON_CAPTURE = false;
    // private static final int PROMOTION_FLAG = 2;

    private final Piece piece;
    private final int from;
    private final int to;
    private final boolean capture;

    private boolean castling;
    private int secondaryFrom;
    private int secondaryTo;
    private Piece promotionPiece;
    private String enPassantTarget = PawnMoves.NO_EN_PASSANT;

    public Move(Piece piece, int from, int to, boolean capture) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.capture = capture;
    }

    public Move(Piece piece, String from, String to, boolean capture) {
        this.piece = piece;
        this.from = FenString.squareToLocation(from);
        this.to = FenString.squareToLocation(to);
        this.capture = capture;
    }

    void updateMoveForCastling(boolean check, String castlingRights) {
        // if it is a castling
        if (!check && getFrom() == 4) { // e1 -> 4
            if (getTo() == 6 && castlingRights.contains("K")) { // g1
                setCastling(7, 5);
            } else if (getTo() == 2 && castlingRights.contains("Q")) { // b1
                setCastling(0, 3);
            }
        } else if (!check && getFrom() == 60) { // g8 || b8
            if (getTo() == 62 && castlingRights.contains("k")) {
                setCastling(63, 61);
            } else if (getTo() == 58 && castlingRights.contains("q")) {
                setCastling(56, 59);
            }
        }
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

    public Piece getPiece() {
        return piece;
    }

    public boolean isCapture() {
        return capture;
    }

    public void updateForEnPassant(long whitePawns, long blackPawns) {
        int location = getFrom();
        int destination = getTo();

        // if the pawn is on their home position && if the destination is two moves away
        if (location >= 8 && location <= 15 && destination - location == 16) { // two moves away

            if (destination < 31 &&
                (((1L << (destination + 1)) & blackPawns) != 0))
                setEnPassant(FenString.locationToSquare(location + 8));

            if (destination > 24 &&
                (((1L << (destination - 1)) & blackPawns) != 0))
                setEnPassant(FenString.locationToSquare(location + 8));

        } else if (location >= 48 && location <= 55 && destination - location == -16) {

            // set the en passant target
            if (destination < 39 &&
                (((1L << (destination + 1)) & whitePawns) != 0))
                setEnPassant(FenString.locationToSquare(location - 8));

            if (destination > 32 &&
                (((1L << (destination - 1)) & whitePawns) != 0))
                setEnPassant(FenString.locationToSquare(location - 8));

        } else {
            // reset the en passant target
            setEnPassant(PawnMoves.NO_EN_PASSANT);
        }
    }

    // updateForEnPassant is the public method to set en passant
    private void setEnPassant(String i) {
        enPassantTarget = i;
    }

    public String getEnPassantTarget() {
        return enPassantTarget;
    }

    public boolean isEnPassant() {
        return !enPassantTarget.equals("-");
    }

    public boolean isCastling() {
        return castling;
    }

    public void setCastling(int secondarySourceLocation, int secondaryDestinationLocation) {
        this.secondaryFrom = secondarySourceLocation;
        this.secondaryTo = secondaryDestinationLocation;
        this.castling = true;
    }

    public boolean isPromoting() {
        return promotionPiece != null;
    }

    public void setPromotion(Piece promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    public int getSecondaryFrom() { return secondaryFrom; }
    public int getSecondaryTo() { return secondaryTo; }
    public Piece getPromotionPiece() { return promotionPiece; }

    public String toLongSan() {
        return FenString.locationToSquare(from) +
            FenString.locationToSquare(to) +
            (promotionPiece != null ? promotionPiece.getPieceChar() : "");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder()
            .append(piece.getPieceChar())
            .append(" : ")
            .append(toLongSan());
        return sb.toString();
    }
}
