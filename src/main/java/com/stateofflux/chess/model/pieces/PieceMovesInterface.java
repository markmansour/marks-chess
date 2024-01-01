package com.stateofflux.chess.model.pieces;

public interface PieceMovesInterface {
    void findCaptureAndNonCaptureMoves();

    long getCaptureMoves();

    long getNonCaptureMoves();
}
