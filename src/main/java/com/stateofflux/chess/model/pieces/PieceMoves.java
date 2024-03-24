package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;

public abstract class PieceMoves implements PieceMovesInterface {
    protected final Board board;
    protected final int location;
    protected final Piece piece;
    protected long nonCaptureMoves;
    protected long captureMoves;
    protected final long occupiedBoard;
    protected long opponentBoard;
    protected final boolean isWhite;

    protected PieceMoves(Board board, int location) {
        this.board = board;
        this.location = location;
        this.isWhite = (((1L << location) & board.getWhite()) != 0);

        // are these always needed? Can we late bind them?
        this.occupiedBoard = this.board.getOccupied();  // the union of current and occupied boards
        this.piece = board.get(location);
        setBoards();
        findCaptureAndNonCaptureMoves();
    }

    protected void setBoards() {
        if (isWhite) {
            this.opponentBoard = board.getBlack();
        } else {
            this.opponentBoard = board.getWhite();
        }
    }

    @Override
    public long getCaptureMoves() {
        return this.captureMoves;
    }

    @Override
    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }
}
