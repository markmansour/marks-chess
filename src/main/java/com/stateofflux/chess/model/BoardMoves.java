package com.stateofflux.chess.model;

public class BoardMoves {
    private final Board board;
    private long emptyBoard;
    private long nonCaptureMoves;
    private long captureMoves;

    // private int location;
    // private PlayerColor color;
    // private long usedBoard;
    // private int max;
    // private Direction[] directions;
    // private Piece piece;

    public static class Builder {
        private Board board;
        private int location = 0;
        // private PlayerColor color;

        // boards
        private long occupiedBoard;
        private long emptyBoard;
        private long opponentBoard;

        private int max = 0;
        private Direction[] directions;
        private Piece piece;
        private long nonCaptureMoves = 0L;
        private long captureMoves = 0L;
        private boolean includeTakingOpponent = false;

        public Builder(Board board, int location) {
            this.board = board;
            this.location = location;

            this.piece = board.getPieceAtLocation(location);

            this.occupiedBoard = this.board.getOccupiedBoard();
            this.emptyBoard = ~occupiedBoard;
            // this.max = 1;
        }

        public Builder moving(Direction[] directions) {
            this.directions = directions;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        // make this the default
        public Builder onlyIfEmpty() {
            return this;
        }

        public Builder onlyIfOpponent() {
            return this;
        }

        public Builder includeTakingOpponent() {
            this.includeTakingOpponent = true;

            this.opponentBoard = switch (this.piece.getColor()) {
                case WHITE -> this.board.getBlackBoard();
                case BLACK -> this.board.getWhiteBoard();
                default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
            };

            return this;
        }

        public BoardMoves build() {
            // validation here - throw IllegalArgumentException with details when invalid
            int nextPosition;
            long nextPositionBit;
            long boardMax;

            // calculate the max moves
            for (Direction d : this.directions) {
                // check to see we're not going off the board.
                boardMax = Math.min(BoardMoves.maxStepsToBoundary(this.location, d), this.max);

                for (int i = 1; i <= boardMax; i++) {
                    // calculate the next position
                    nextPosition = this.location + (i * d.getDistance());
                    nextPositionBit = 1L << nextPosition;

                    // if the next position is empty, add it to the bitmap
                    if ((this.occupiedBoard & nextPositionBit) == 0)
                        this.nonCaptureMoves |= nextPositionBit;

                    // if the next position is occupied, add it to the bitmap and stop
                    if (this.includeTakingOpponent && (this.opponentBoard & nextPositionBit) != 0) {
                        this.captureMoves |= nextPositionBit;
                        break; // no more searches in this direction.
                    }
                }
            }
            return new BoardMoves(this);
        }
    }

    public static int maxStepsToBoundary(int location, Direction direction) {
        return switch (direction) {
            case RIGHT, LEFT, DOWN, UP ->
                maxStepsToBoundaryHoriztonalOrVertical(location, direction);
            case UP_LEFT ->
                Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                        maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case UP_RIGHT ->
                Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                        maxStepsToBoundaryHoriztonalOrVertical(location, Direction.RIGHT));
            case DOWN_LEFT ->
                Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
                        maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case DOWN_RIGHT ->
                Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
                        maxStepsToBoundaryHoriztonalOrVertical(location, Direction.RIGHT));
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        };
    }

    public static int maxStepsToBoundaryHoriztonalOrVertical(int location, Direction direction) {
        return switch (direction) {
            case RIGHT -> 7 - (location % 8);
            case LEFT -> location % 8;
            case DOWN -> location / 8;
            case UP -> 7 - (location / 8);
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        };
        // check to see we're not going off the board.
    }

    private BoardMoves(Builder builder) {
        this.board = builder.board;
        this.nonCaptureMoves = builder.nonCaptureMoves;
        this.captureMoves = builder.captureMoves;
    }

    public long getAllMoves() {
        return 0;
    }

    public long getCaptureMoves() {
        return this.captureMoves;
    }

    public long getNonCaptureMoves() {
        return this.nonCaptureMoves;
    }
}
