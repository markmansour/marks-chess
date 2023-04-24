package com.stateofflux.chess.model;

public class BoardMoves {
    private final Board board;
    private long nonCaptureMoves;
    private long captureMoves;
    private Piece piece;

    // Factory for board moves.
    protected static BoardMoves from(Board b, int location) {
        Piece piece = b.getPieceAtLocation(location);

        return switch (piece) {
            case BLACK_KING, WHITE_KING -> KingMoves.from(b, location);
            case BLACK_QUEEN, WHITE_QUEEN -> QueenMoves.from(b, location);
            case BLACK_ROOK, WHITE_ROOK -> RookMoves.from(b, location);
            case BLACK_BISHOP, WHITE_BISHOP -> BishopMoves.from(b, location);
            case BLACK_KNIGHT, WHITE_KNIGHT -> KnightMoves.from(b, location);
            case BLACK_PAWN, WHITE_PAWN -> PawnMoves.from(b, location);
            default -> BoardMoves.from(b, location);
        };
    }

    public static class Builder {
        private Board board;
        private int location = 0;

        // boards
        private long occupiedBoard;
        private long opponentBoard;

        private int max = 7; // max number of moves in any direction
        private Direction[] directions;
        private Piece piece;
        private long nonCaptureMoves = 0L;
        private long captureMoves = 0L;

        public Builder(Board board, int location) {
            this.board = board;
            this.location = location;
            this.piece = board.getPieceAtLocation(location);
            this.occupiedBoard = this.board.getOccupiedBoard();
        }

        public BoardMoves build() {
            // set the defaults if not set by user
            if (this.directions == null)
                throw new IllegalArgumentException("Directions must be set for the piece");

            includeCaptureMoves();
            findMovesInStraightLines();

            return new BoardMoves(this);
        }

        /**
         * @implSpec override this method to set the directions allowed for the piece.
         */
        public Builder moveAndCaptureDirections(Direction[] direction) {
            this.directions = direction;

            return this;
        }

        protected Builder max(int max) {
            this.max = max;
            return this;
        }

        protected Builder includeCaptureMoves() {
            this.opponentBoard = switch (this.piece.getColor()) {
                case WHITE -> this.board.getBlackBoard();
                case BLACK -> this.board.getWhiteBoard();
                default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
            };

            return this;
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
                    if ((this.opponentBoard & nextPositionBit) != 0) {
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

    protected BoardMoves(Builder builder) {
        // need to clone enums? See Item 50 in Effective Java
        this.board = builder.board;
        this.piece = builder.piece;

        this.nonCaptureMoves = builder.nonCaptureMoves;
        this.captureMoves = builder.captureMoves;
    }

    public Board getBoard() {
        return this.board;
    }

    public Piece getPiece() {
        return this.piece;
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
