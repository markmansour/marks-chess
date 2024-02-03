package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.PawnMoves;
import com.stateofflux.chess.model.pieces.Piece;

import java.util.Objects;

public class Move {
    public static final boolean CAPTURE = true;
    public static final boolean NON_CAPTURE = false;
    // private static final int PROMOTION_FLAG = 2;

    private final Piece piece;
    private final int from;
    private final int to;
    private final boolean capture;
    private Piece capturePiece;

    private boolean castling;
    private int secondaryFrom;
    private int secondaryTo;
    private Piece promotionPiece;
    private int enPassantTarget = PawnMoves.NO_EN_PASSANT_VALUE;

    public Move(Piece piece, int from, int to, boolean capture) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.capture = capture;
    }

    public Move(Piece piece, int from, int to, boolean capture, Piece promotionPiece) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.capture = capture;
        this.promotionPiece = promotionPiece;
    }

    public Move(Piece piece, String from, String to, boolean capture) {
        this.piece = piece;
        this.from = FenString.squareToLocation(from);
        this.to = FenString.squareToLocation(to);
        this.capture = capture;
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

    public Piece getCapturePiece() {
        return capturePiece;
    }

    public void setCapturePiece(Piece capturePiece) {
        this.capturePiece = capturePiece;
    }

    public void updateForEnPassant(long whitePawns, long blackPawns) {
        int location = getFrom();
        int destination = getTo();

        // if the pawn is on their home position && if the destination is two moves away
        if (location >= 8 && location <= 15 && destination - location == 16) { // two moves away

            if (destination < 31 &&
                (((1L << (destination + 1)) & blackPawns) != 0))  // black pawn on the east
                setEnPassant(location + 8);

            if (destination > 24 &&
                (((1L << (destination - 1)) & blackPawns) != 0))  // black pawn on the west
                setEnPassant(location + 8);

        } else if (location >= 48 && location <= 55 && destination - location == -16) {

            // set the en passant target
            if (destination < 39 &&
                (((1L << (destination + 1)) & whitePawns) != 0))
                setEnPassant(location - 8);

            if (destination > 32 &&
                (((1L << (destination - 1)) & whitePawns) != 0))
                setEnPassant(location - 8);

        } else {
            // reset the en passant target
            clearEnPassant();
        }
    }

    // updateForEnPassant is the public method to set en passant
    public void setEnPassant(int i) {
        enPassantTarget = i;
    }

    public int getEnPassantTarget() {
        return enPassantTarget;
    }

    public boolean isEnPassant() {
        return enPassantTarget != PawnMoves.NO_EN_PASSANT_VALUE;
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
        if(promotionPiece == null)
            return;

        if((piece.isWhite() && promotionPiece.isWhite()) ||
            piece.isBlack() && promotionPiece.isBlack())
            this.promotionPiece = promotionPiece;
        else
            this.promotionPiece = promotionPiece.inOpponentsColor();  // the case used for promotion is unexpected.
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
        return piece.getPieceChar() +
            " : " +
            toLongSan();
    }

    public void clearEnPassant() {
        setEnPassant(PawnMoves.NO_EN_PASSANT_VALUE);
    }

    public boolean isEnPassantCapture(int gameEnPassantState) {
        return piece.isPawn() &&
            (Board.file(getFrom()) != Board.file(getTo())) &&
            gameEnPassantState != PawnMoves.NO_EN_PASSANT_VALUE &&
            getTo() == gameEnPassantState;
    }

    // Capture > NonCapture
    public int getComparisonValue() {
        return isCapture() ? 1 : 0;
    }
}
