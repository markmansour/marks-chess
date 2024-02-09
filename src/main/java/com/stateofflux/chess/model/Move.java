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
    private Piece capturePiece;

    private boolean castling;
    private int secondaryFrom;
    private int secondaryTo;
    private Piece promotionPiece;
    private int enPassantTarget;

    public Move(Piece piece, String from, String to, boolean capture) {
        this(piece, FenString.squareToLocation(from), FenString.squareToLocation(to), capture);
    }

    public Move(Piece piece, int from, int to, boolean capture) {
        this(piece, from, to, capture, Piece.EMPTY);
    }

    public Move(Piece piece, int from, int to, boolean capture, Piece promotionPiece) {
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.capture = capture;
        this.capturePiece = Piece.EMPTY;
        this.castling = false;
        this.secondaryFrom = 0;
        this.secondaryTo = 0;
        this.promotionPiece = promotionPiece;
        this.enPassantTarget = PawnMoves.NO_EN_PASSANT_VALUE;
    }

    /*
     *                                      offset
     * piece            - 0-12  -  4 bits -  0
     * from             - 0-63  -  8 bits -  4
     * to               - 0-63  -  8 bits - 12
     * secondary from   - 0-63  -  8 bits - 20
     * secondary to     - 0-63  -  8 bits - 28
     * enPassant target - 0-64  - 16 bits - 36
     * promotion piece  - 0-12  -  4 bits - 48
     * capture piece    - 0-12  -  4 bits - 52
     * castling         - 0-1   -  1 bit  - 56
     *                            -------
     *                            57 bits
     */
    public long toLong() {
        long hash = 0;

        hash |= this.getPiece().getIndex();
        hash |= ((long) this.getFrom()) << 4;
        hash |= ((long) this.getTo()) << 12;
        hash |= ((long) this.getSecondaryFrom()) << 20;
        hash |= ((long) this.getSecondaryTo()) << 28;
        hash |= ((long) this.getEnPassantTarget()) << 36;

        if(isPromoting())
            hash |= ((long) this.getPromotionPiece().getIndex()) << 48;
        else
            hash |= ((long) Piece.EMPTY.getIndex()) << 48;

        if(isCapture())
           hash |= ((long) this.getCapturePiece().getIndex()) << 52;
        else
            hash |= ((long) Piece.EMPTY.getIndex()) << 52;

        hash |= ((long) (this.isCastling() ? 1 : 0)) << 56;

        return hash;
    }

    public static Move buildFrom(long hash) {
        Piece piece = Piece.getPieceByIndex((int) (hash & 0xF));
        int from = (int) ((hash >> 4) & 0x3F);
        int to = (int) ((hash >> 12) & 0x3F);
        int secondaryFrom = (int) ((hash >> 20) & 0x3F);
        int secondaryTo = (int) ((hash >> 28) & 0x3F);
        int enPassantTarget = (int) ((hash >> 36) & 0x7F);
        Piece promotionPiece = Piece.getPieceByIndex((int) ((hash >> 48) & 0xF));
        Piece capturePiece = Piece.getPieceByIndex((int) ((hash >> 52) & 0xF));
        boolean castling = ((hash >> 56) & 1) == 1;

        Move m = new Move(piece, from, to, !capturePiece.isEmpty());

        if(castling)
            m.setCastling(secondaryFrom, secondaryTo);

        if(!promotionPiece.isEmpty())
            m.setPromotion(promotionPiece);

        if(castling)
            m.setCastling(secondaryFrom, secondaryTo);

        if(enPassantTarget != PawnMoves.NO_EN_PASSANT_VALUE)
            m.setEnPassant(enPassantTarget);

        if(!capturePiece.isEmpty())
            m.setCapturePiece(capturePiece);

        return m;
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
        return promotionPiece != Piece.EMPTY;
    }

    public void setPromotion(Piece promotionPiece) {
        if(promotionPiece == Piece.EMPTY) {
            this.promotionPiece = Piece.EMPTY;
            return;
        }

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
            (promotionPiece != Piece.EMPTY ? promotionPiece.getPieceChar() : "");
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
        return
            gameEnPassantState != PawnMoves.NO_EN_PASSANT_VALUE &&
            piece.isPawn() &&
            (Board.file(getFrom()) != Board.file(getTo())) &&
            getTo() == gameEnPassantState;
    }

    // Capture > NonCapture
    public int getComparisonValue() {
        return isCapture() ? 1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (from != move.from) return false;
        if (to != move.to) return false;
        if (capture != move.capture) return false;
        if (castling != move.castling) return false;
        if (secondaryFrom != move.secondaryFrom) return false;
        if (secondaryTo != move.secondaryTo) return false;
        if (enPassantTarget != move.enPassantTarget) return false;
        if (piece != move.piece) return false;
        if (capturePiece != move.capturePiece) return false;
        return promotionPiece == move.promotionPiece;
    }

    @Override
    public int hashCode() {
        int result = piece.hashCode();
        result = 31 * result + from;
        result = 31 * result + to;
        result = 31 * result + (capture ? 1 : 0);
        result = 31 * result + (capturePiece != null ? capturePiece.hashCode() : 0);
        result = 31 * result + (castling ? 1 : 0);
        result = 31 * result + secondaryFrom;
        result = 31 * result + secondaryTo;
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
        result = 31 * result + enPassantTarget;
        return result;
    }
}
