package com.stateofflux.chess.model;

public abstract class BoardMoves {
    private final Board board;
    // private long emptyBoard;
    private long nonCaptureMoves;
    private long captureMoves;

    abstract static class Builder<T extends Builder<T>> {
        protected Board board;
        protected int location = 0;
        // protected PlayerColor color;

        // boards
        protected long occupiedBoard;
        // private long emptyBoard;
        protected long opponentBoard;

        protected int max = 7; // max number of moves in any direction
        protected Direction[] directions;
        protected Piece piece;
        protected long nonCaptureMoves = 0L;
        protected long captureMoves = 0L;
        protected boolean includeTakingOpponent = false;
        protected boolean takingOpponentSetByUser = false;

        protected Builder(Board board, int location) {
            this.board = board;
            this.location = location;

            this.piece = board.getPieceAtLocation(location);

            this.occupiedBoard = this.board.getOccupiedBoard();
            // this.emptyBoard = ~occupiedBoard;
        }

        // Subclasses must override this method to return "this"
        protected abstract T self();

        protected abstract BoardMoves getInstance();

        protected BoardMoves build() {
            // set the defaults if not set by user
            if (!takingOpponentSetByUser)
                includeCaptureMoves(true);

            allowedDirections();
            findMovesInStraightLines();

            return getInstance();
        }

        /**
         * @implSpec override this method to set the directions allowed for the piece.
         */
        protected void allowedDirections() {
            this.directions = new Direction[] {
                    Direction.UP_LEFT,
                    Direction.UP,
                    Direction.UP_RIGHT,
                    Direction.RIGHT,
                    Direction.DOWN_RIGHT,
                    Direction.DOWN,
                    Direction.DOWN_LEFT,
                    Direction.LEFT

            };
        }

        protected Builder<T> moving(Direction[] directions) {
            this.directions = directions;
            return self();
        }

        protected Builder<T> max(int max) {
            this.max = max;
            return self();
        }

        /*
         * // make this the default
         * public Builder<T> onlyIfEmpty() {
         * return this;
         * }
         *
         * public Builder<T> onlyIfOpponent() {
         * return this;
         * }
         */

        protected Builder<T> includeCaptureMoves(boolean flag) {
            this.includeTakingOpponent = flag;
            this.takingOpponentSetByUser = true;

            if (includeTakingOpponent) {
                this.opponentBoard = switch (this.piece.getColor()) {
                    case WHITE -> this.board.getBlackBoard();
                    case BLACK -> this.board.getWhiteBoard();
                    default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
                };
            }

            return self();
        }

        protected long getCurrentPlayerBoard() {
            return switch (this.piece.getColor()) {
                case WHITE -> this.board.getWhiteBoard();
                case BLACK -> this.board.getBlackBoard();
                default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
            };
        }

        public void findMovesInStraightLines() {
            // validation here - throw IllegalArgumentException with details when invalid
            int nextPosition;
            long nextPositionBit;
            long boardMax;
            long currentPlayerBoard = getCurrentPlayerBoard(); // should this be an instance var?

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

                    // if the next position is the same color, stop
                    if ((currentPlayerBoard & nextPositionBit) != 0)
                        break; // stop looking in the current direction

                    // if the next position is occupied, add it to the bitmap and stop
                    if (this.includeTakingOpponent && (this.opponentBoard & nextPositionBit) != 0) {
                        this.captureMoves |= nextPositionBit;
                        break; // no more searches in this direction.
                    }
                }
            }
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

    protected BoardMoves(Builder<?> builder) {
        // need to clone enums? See Item 50 in Effective Java
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
