package com.stateofflux.chess.model;

public abstract class BoardMoves {
    protected final Board board;
    protected int location;
    protected Piece piece;
    protected long nonCaptureMoves;
    protected long captureMoves;
    protected long occupiedBoard;
    protected long opponentBoard;
    protected long currentPlayerBoard;

    // Factory for board moves.
    // protected static BoardMoves from(Board b, int location) {
    // }

    protected BoardMoves(Board board, int location) {
        this.board = board;
        this.location = location;
        this.piece = board.getPieceAtLocation(location);

        // are these always needed? Can we late bind them?
        this.occupiedBoard = this.board.getOccupiedBoard();
        this.opponentBoard = getOpponentBoard(); // calculation
        this.currentPlayerBoard = getCurrentPlayerBoard();

        setupPaths();
        findCaptureAndNonCaptureMoves();
    }

    abstract void setupPaths();

    abstract void findCaptureAndNonCaptureMoves();

    protected long getOpponentBoard() {
        return switch (this.piece.getColor()) {
            case WHITE -> this.board.getBlackBoard();
            case BLACK -> this.board.getWhiteBoard();
            default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
        };
    }

    protected long getCurrentPlayerBoard() {
        return switch (this.piece.getColor()) {
            case WHITE -> this.board.getWhiteBoard();
            case BLACK -> this.board.getBlackBoard();
            default -> throw new IllegalArgumentException("Unexpected value: " + this.piece.getColor());
        };
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

    // ---------------------------- static utilities ----------------------------
    public static int maxStepsToBoundary(int location, Direction direction) {
        return switch (direction) {
            case RIGHT, LEFT, DOWN, UP -> maxStepsToBoundaryHoriztonalOrVertical(location, direction);
            case UP_LEFT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case UP_RIGHT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.UP),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.RIGHT));
            case DOWN_LEFT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
                    maxStepsToBoundaryHoriztonalOrVertical(location, Direction.LEFT));
            case DOWN_RIGHT -> Math.min(maxStepsToBoundaryHoriztonalOrVertical(location, Direction.DOWN),
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

}
