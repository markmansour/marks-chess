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

    /*
     * private static final long RANK = 255L;
     * private static final long RANK_2 = RANK << 8;
     * private static final long RANK_4 = RANK << 24;
     * private static final long RANK_5 = RANK << 32;
     * private static final long RANK_7 = RANK << 48;
     */

    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;
    protected Map<String, PieceMoves> nextMoves;
    protected int nextMovesCount;
    protected int sourceLocation = -1;
    protected int destinationLocation = -1;

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

    public int getEnPassantTargetAsInt() {
        return this.enPassantTarget;
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

            PieceMoves bm = piece.generateMoves(this.board, i, getCastlingRights(), getEnPassantTargetAsInt());
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
                findSourceAndDestination(action);
                this.board.move(this.sourceLocation, this.destinationLocation);
            }
            case 'B' -> {
                // bishop move
                // TODO validate bishop move
                findSourceAndDestination(action);
                this.board.move(this.sourceLocation, this.destinationLocation);
            }
            case 'R' -> {
                // rook move
                findSourceAndDestination(action);
                this.board.move(this.sourceLocation, this.destinationLocation);
            }
            case 'Q' -> {
                // queen move
                findSourceAndDestination(action);
                this.board.move(this.sourceLocation, this.destinationLocation);
            }
            case 'K' -> {
                // king move
                findSourceAndDestination(action);
                this.board.move(this.sourceLocation, this.destinationLocation);
            }
            default -> {
                // can this be moved into the findSourceAndDestination method?
                // pawn move
                int destination = FenString.squareToLocation(action);
                locations = this.board.getPawnLocations(this.getActivePlayerColor());
                boolean moved = false;

                // TODO: this can be done smarter. if we know the destination (from the action),
                // then
                // we don't need to iterate through all the locations and instead we can take
                // the pawnlocatin that
                // is in the same file as the destination.
                for (int i : locations) {
                    Piece piece = this.board.getPieceAtLocation(i);
                    PieceMoves bm = piece.generateMoves(this.board, i, getCastlingRights(), getEnPassantTargetAsInt());

                    if ((bm.getNonCaptureMoves() & (1L << destination)) != 0) {
                        this.board.move(i, destination);
                        moved = true;
                    } else if ((bm.getCaptureMoves() & (1L << destination)) != 0) {
                        // normal capture
                        this.board.move(i, destination);
                        moved = true;
                    } else {
                        moved = false;
                    }

                    // update the en passant value
                    if (moved) {
                        // if the pawn is on their home position && if the destination is two moves away
                        if (i >= 8 && i <= 15 && destination - i == 16) { // two moves away

                            if (destination < 31 &&
                                    (((1L << (destination + 1)) & this.board.getBlackPawnBoard()) != 0))
                                this.setEnPassantTarget(FenString.locationToSquare(i + 8));

                            if (destination > 24 &&
                                    (((1L << (destination - 1)) & this.board.getBlackPawnBoard()) != 0))
                                this.setEnPassantTarget(FenString.locationToSquare(i + 8));

                        } else if (i >= 48 && i <= 55 && destination - i == -16) {

                            // set the en passant target
                            if (destination < 39 &&
                                    (((1L << (destination + 1)) & this.board.getWhitePawnBoard()) != 0))
                                this.setEnPassantTarget(FenString.locationToSquare(i - 8));

                            if (destination > 32 &&
                                    (((1L << (destination - 1)) & this.board.getWhitePawnBoard()) != 0))
                                this.setEnPassantTarget(FenString.locationToSquare(i - 8));

                        } else {
                            // reset the en passant target
                            this.setEnPassantTarget(PawnMoves.NO_EN_PASSANT);
                        }
                    }
                    // update the castling rights
                }
            }
        }

        removeCastlingRightsFor(this.sourceLocation);

        // TODO update board
        // TODO update game state
        // TODO update next moves
        if (this.activePlayerColor == PlayerColor.WHITE) {
            this.activePlayerColor = PlayerColor.BLACK;
        } else {
            this.activePlayerColor = PlayerColor.WHITE;
        }
    }

    // Examples:
    // d2 - pawns may have no char to represent them
    // Nd4 - non pawns will have thier type as the first character
    // Nxb8 - captures will have an x
    // cxb5 - pawn captures will start with the file
    // Nxc8+ - check
    // Nxc8* - checkmate
    // O-O - king side castle
    // O-O-O - queen side castle
    // e8=Q - pawn promotion
    //
    // need to work backward from the destination to the source.
    // 1. the piece type can be derived from the action (first char)
    // 2. source has to be derived from the destination
    //
    private void findSourceAndDestination(String action) {
        // when there is ambiguity
        // [https://www.chessprogramming.org/Algebraic_Chess_Notation#Ambiguities], use
        // the following rules in order:
        // 1. file of departure if different
        // 2. rank of departure if the files are the same but the ranks differ
        // 3. the complete origin square coordinate otherwise
        char[] actionChars = action.toCharArray();
        int destination = FenString.squareToLocation(action);
        int source = -1;
        int possibleSourceLocations[];
        boolean capture = false;

        // if king side castle
        // if queenside castle
        // if pawn promotion
        // if non-pawn
        // if pawn

        switch (actionChars[0]) {
            case 'N' -> {
                possibleSourceLocations = this.board.getKnightLocations(this.getActivePlayerColor());
            }
            case 'B' -> {
                possibleSourceLocations = this.board.getBishopLocations(this.getActivePlayerColor());
            }
            case 'R' -> {
                possibleSourceLocations = this.board.getRookLocations(this.getActivePlayerColor());
            }
            case 'Q' -> {
                possibleSourceLocations = this.board.getQueenLocations(this.getActivePlayerColor());
            }
            case 'K' -> {
                possibleSourceLocations = this.board.getKingLocations(this.getActivePlayerColor());
            }
            default -> {
                possibleSourceLocations = new int[0];
            }
        }

        char rankSpecified = 0;
        char fileSpecified = 0;

        if (action.length() == 5 && action.charAt(2) == 'x') {
            if (action.charAt(1) >= 'a' && action.charAt(1) <= 'h')
                fileSpecified = action.charAt(1);
            else if (action.charAt(1) >= '1' && action.charAt(1) <= '8')
                rankSpecified = action.charAt(1);
        }

        int tempLocation;
        for (int i = 0; i < possibleSourceLocations.length && source == -1; i++) {
            tempLocation = possibleSourceLocations[i];
            Piece piece = this.board.getPieceAtLocation(tempLocation);
            PieceMoves bm = piece.generateMoves(this.board, tempLocation, getCastlingRights(),
                    getEnPassantTargetAsInt());

            // playing non-capture move
            if ((bm.getNonCaptureMoves() & (1L << destination)) != 0) {
                source = tempLocation;
            } else if ((bm.getCaptureMoves() & (1L << destination)) != 0) {
                if (rankSpecified == 0 && fileSpecified == 0) {
                    source = tempLocation;
                    capture = true;
                } else {
                    // verify the source as there are multiple pieces that make a capture
                    if (rankSpecified == '1' + (tempLocation / 8) ||
                            fileSpecified == 'a' + (tempLocation % 8)) {
                        source = tempLocation;
                        capture = true;
                    }
                }
            }
        }

        // TODO: replace with getters/setters
        this.sourceLocation = source;
        this.destinationLocation = destination;
    }

    private void removeCastlingRightsFor(int i) {
        this.castlingRights = switch (i) {
            case 0 -> {
                yield this.castlingRights.replace("Q", "");
            }
            case 4 -> {
                yield this.castlingRights.replace("K", "").replace("Q", "");
            }
            case 7 -> {
                yield this.castlingRights.replace("K", "");
            }
            case 56 -> {
                yield this.castlingRights.replace("q", "");
            }
            case 60 -> {
                yield this.castlingRights.replace("k", "").replace("q", "");
            }
            case 63 -> {
                yield this.castlingRights.replace("k", "");
            }
            default -> {
                yield this.castlingRights;
            }
        };
    }

    /*
     * <FEN> ::= <Piece Placement>
     * ' ' <Side to move>
     * ' ' <Castling ability>
     * ' ' <En passant target square>
     * ' ' <Halfmove clock>
     * ' ' <Fullmove counter>
     */
    public String asFen() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPiecePlacement());
        sb.append(' ');
        sb.append(this.getActivePlayerColor());
        sb.append(' ');
        sb.append(this.getCastlingRights());
        sb.append(' ');
        sb.append(this.getEnPassantTarget());
        sb.append(' ');
        sb.append(this.getHalfmoveClock());
        sb.append(' ');
        sb.append(this.getFullmoveCounter());
        return sb.toString();
    }

    public String asFenNoCounters() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPiecePlacement());
        sb.append(' ');
        sb.append(this.getActivePlayerColor());
        sb.append(' ');
        sb.append(this.getCastlingRights());
        sb.append(' ');
        sb.append(this.getEnPassantTarget());
        return sb.toString();
    }
}
