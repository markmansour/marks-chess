package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public abstract class StraightLineMoves extends PieceMoves {

    protected int max = 7; // max number of moves in any direction
    protected Direction[] directions;

    protected StraightLineMoves(Board board, int location) {
        super(board, location);
    }

    protected void findCaptureAndNonCaptureMoves() {
        findCaptureAndNonCaptureMovesInStraightLines();
    }

    public boolean isCheckingForCaptures() {
        return true;
    }

    public void findCaptureAndNonCaptureMovesInStraightLines() {
        // validation here - throw IllegalArgumentException with details when invalid
        int nextPosition;
        long nextPositionBit;
        int boardMax;

        // calculate the max moves
        for (Direction d : this.directions) {
            // check to see we're not going off the board.
            boardMax = Math.min(PieceMoves.maxStepsToBoundary(this.location, d), this.max);

            for (int i = 1; i <= boardMax; i++) {
                // calculate the next position
                nextPosition = this.location + (i * d.getDistance());
                nextPositionBit = 1L << nextPosition;

                // if the next position is empty, add it to the bitmap
                if ((this.occupiedBoard & nextPositionBit) == 0)
                    this.nonCaptureMoves |= nextPositionBit;

                // if the next position is the same color, stop
                if ((currentPlayerBoard & nextPositionBit) != 0)
                    break; // stop looking in the current direction

                // this is only used by the pawn.
                if (!this.isCheckingForCaptures()) {
                    if ((opponentBoard & nextPositionBit) != 0)
                        break;  // if the piece in front of the pawn is an opponent piece, then stop moving in this direction
                    else
                        continue;  // the next space is empty, so keep going in the same direction.
                }

                // if the next position is occupied, add it to the bitmap and stop
                if ((this.opponentBoard & nextPositionBit) != 0) {
                    this.captureMoves |= nextPositionBit;

                    break; // no more searches in this direction.
                }
            }
        }
    }
}
