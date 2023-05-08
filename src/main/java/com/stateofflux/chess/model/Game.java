package com.stateofflux.chess.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stateofflux.chess.model.pieces.PawnMoves;
import com.stateofflux.chess.model.pieces.Piece;
import com.stateofflux.chess.model.pieces.PieceMoves;

/*
 * Only legal moves can happen through the Game object.  It acts as the
 * validation layer of the Board object.
 */
public class Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;
    protected Map<String, PieceMoves> nextMoves;
    protected int nextMovesCount;

    // If neither side has the ability to castle, this field uses the character "-".
    // Otherwise, this field contains one or more letters: "K" if White can castle
    // kingside, "Q" if White can castle queenside, "k" if Black can castle
    // kingside, and "q" if Black can castle queenside. A situation that temporarily
    // prevents castling does not prevent the use of this notation.
    protected String castlingRights;

    // En passant target square
    protected int enPassantTarget;

    // The number of halfmoves since the last capture or pawn advance, used for the
    // fifty-move rule
    protected int halfmoveClock;

    // The number of the full moves. It starts at 1 and is incremented after Black's
    // move
    protected int fullmoveCounter;

    // game with players - intended for play
    public Game(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.board = new Board();
    }

    // game with no players - used for analysis
    public Game() {
        this.board = new Board();
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setCastlingRights("KQkq");
        this.setEnPassantTarget("-");
        this.setHalfmoveClock(0);
        this.setFullmoveCounter(1);
    }

    // game that can start midway through - used for analysis
    public Game(String fenString) {
        FenString fen = new FenString(fenString);
        this.board = new Board(fen.getPiecePlacement());
        this.setActivePlayerColor(fen.getActivePlayerColor());
        this.setCastlingRights(fen.getCastlingRights());
        this.setEnPassantTarget(fen.getEnPassantTarget());
        this.setHalfmoveClock(fen.getHalfmoveClock());
        this.setFullmoveCounter(fen.getFullmoveCounter());
    }

    public String getPiecePlacement() {
        return this.board.toFenPiecePlacementString();
    }

    private void setFullmoveCounter(int fullmoveCounter) {
        this.fullmoveCounter = fullmoveCounter;
    }

    public int getFullmoveCounter() {
        return this.fullmoveCounter;
    }

    private void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    public int getHalfmoveClock() {
        return this.halfmoveClock;
    }

    private void setEnPassantTarget(String target) {
        if (target.equals(PawnMoves.NO_EN_PASSANT)) {
            this.enPassantTarget = -1;
        } else {
            this.enPassantTarget = FenString.squareToLocation(target);
        }
    }

    public String getEnPassantTarget() {
        if (this.enPassantTarget == -1) {
            return "-";
        }

        return FenString.locationToSquare(enPassantTarget);
    }

    private void setCastlingRights(String castlingRights) {
        this.castlingRights = castlingRights;
    }

    public String getCastlingRights() {
        return this.castlingRights;
    }

    private void setActivePlayerColor(PlayerColor activePlayerColor) {
        this.activePlayerColor = activePlayerColor;
    }

    public PlayerColor getActivePlayerColor() {
        return this.activePlayerColor;
    }

    // iterate over all pieces on the board
    // for each piece, generate the moves for that piece
    // store the moves in a list
    // return number of moves
    // 1 ply
    public int generateMoves() {
        this.nextMoves = new HashMap<>();
        this.nextMovesCount = 0;

        // TODO find a faster way to iterate over the board.
        for (int i = 0; i < 64; i++) {
            Piece piece = this.board.getPieceAtLocation(i);

            if (piece == Piece.EMPTY || piece.getColor() != this.activePlayerColor)
                continue;

            PieceMoves bm = piece.generateMoves(this.board, i);
            if (bm.getMovesCount() > 0) {
                this.nextMoves.put(FenString.locationToSquare(i), bm);
                LOGGER.info("Generated {} moves for {} at {}", bm.getMovesCount(), piece, i);
                this.nextMovesCount += bm.getMovesCount();
            }
        }

        return this.nextMovesCount;
    }

    public Map<String, PieceMoves> getNextMoves() {
        return this.nextMoves;
    }

    // using chess algebraic notation
    public void move(String action) {
        int[] locations;
        // TODO validate action

        // TODO validate move

        // test if the action is valid
        switch (action.charAt(0)) {
            case 'N' -> {
                // knight move
                // TODO validate knight move
            }
            case 'B' -> {
                // bishop move
                // TODO validate bishop move
            }
            case 'R' -> {
                // rook move
                int destination = FenString.squareToLocation(action);
                locations = this.board.getRookLocations(this.getActivePlayerColor());

                for (int i : locations) {
                    Piece piece = this.board.getPieceAtLocation(i);
                    PieceMoves bm = piece.generateMoves(this.board, i);

                    // playing non-capture move
                    if ((bm.getNonCaptureMoves() & (1L << destination)) != 0) {
                        this.board.move(i, destination);
                        removeCastlingRightsFor(i);
                    } else if ((bm.getCaptureMoves() & (1L << destination)) != 0) {
                        // normal capture
                        this.board.move(i, destination);
                        removeCastlingRightsFor(i);
                    }
                }
            }
            case 'Q' -> {
                // queen move
                // TODO validate queen move
            }
            case 'K' -> {
                // king move
                // TODO validate king move
            }
            default -> {
                // pawn move
                int destination = FenString.squareToLocation(action);
                locations = this.board.getPawnLocations(this.getActivePlayerColor());

                for (int i : locations) {
                    Piece piece = this.board.getPieceAtLocation(i);
                    PieceMoves bm = piece.generateMoves(this.board, i);

                    // playing non-capture move
                    if ((bm.getNonCaptureMoves() & (1L << destination)) != 0)
                        this.board.move(i, destination);
                    else if ((bm.getCaptureMoves() & (1L << destination)) != 0) {
                        // normal capture
                        this.board.move(i, destination);
                        // TODO: en passant - remove the taken piece
                    }
                }
            }
        }

        // TODO update board
        // TODO update game state
        // TODO update next moves
        if (this.activePlayerColor == PlayerColor.WHITE)

        {
            this.activePlayerColor = PlayerColor.BLACK;
        } else {
            this.activePlayerColor = PlayerColor.WHITE;
        }
    }

    private void removeCastlingRightsFor(int i) {
        this.castlingRights = switch (i) {
            case 0 -> {
                yield this.castlingRights.replace("Q", "");
            }
            case 7 -> {
                yield this.castlingRights.replace("K", "");
            }
            case 56 -> {
                yield this.castlingRights.replace("q", "");
            }
            case 63 -> {
                yield this.castlingRights.replace("k", "");
            }
            default -> {
                yield this.castlingRights;
            }
        };
    }
}
