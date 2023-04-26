package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Board;
import com.stateofflux.chess.model.Direction;

public class PawnMoves extends StraightLineMoves {
    protected Direction[] captureDirections;

    public PawnMoves(Board board, int location) {
        super(board, location);
    }

    protected void setupPaths() {
        this.max = 2;
        if (this.piece.isBlack()) {
            this.directions = new Direction[] { Direction.DOWN };
            if (this.location < 48) { // no longer on rank 7
                this.max = 1;
            }

            this.captureDirections = switch (this.location % 8) {
                case 7 -> {
                    yield new Direction[] { Direction.DOWN_LEFT };
                }
                case 0 -> {
                    yield new Direction[] { Direction.DOWN_RIGHT };
                }
                default -> {
                    yield new Direction[] { Direction.DOWN_LEFT, Direction.DOWN_RIGHT };
                }
            };
        } else if (this.piece.isWhite()) {
            this.directions = new Direction[] { Direction.UP };
            if (this.location >= 16) { // no longer on rank 2
                this.max = 1;
            }

            this.captureDirections = switch (this.location % 8) {
                case 7 -> {
                    yield new Direction[] { Direction.UP_LEFT };
                }
                case 0 -> {
                    yield new Direction[] { Direction.UP_RIGHT };
                }
                default -> {
                    yield new Direction[] { Direction.UP_LEFT, Direction.UP_RIGHT };
                }
            };
        } else {
            throw new IllegalArgumentException("Piece must be black or white");
        }
    }

    @Override
    public boolean checkForCaptures() {
        return false;
    }

    // calculate the non capture moves for a pawn
    @Override
    public void findCaptureAndNonCaptureMoves() {
        findCaptureAndNonCaptureMovesInStraightLines();
        findPawnCaptures();
    }

    private void findPawnCaptures() {
        int nextPosition;
        long nextPositionBit;

        for (Direction d : this.captureDirections) {
            nextPosition = this.location + d.getDistance();
            nextPositionBit = 1L << nextPosition;

            if ((this.opponentBoard & nextPositionBit) != 0) {
                this.captureMoves |= nextPositionBit;
            }
        }
    }
}
