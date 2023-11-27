package com.stateofflux.chess.model;

import java.util.*;

import com.stateofflux.chess.model.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Only legal moves can happen through the Game object.  It acts as the
 * validation layer of the Board object.
 */
public class Game {
    // TODO replace most integers with bytes to save space
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
    protected MoveList<Move> activePlayerMoves;
    protected MoveList<Move> otherPlayerMoves;
    protected int sourceLocation = -1;
    protected int destinationLocation = -1;
    private int secondarySourceLocation = -1;
    private int secondaryDestinationLocation = -1;
    private boolean limitMovesTo50 = true;
    private int depth = 0;

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
    private boolean outOfTime = false;
    private boolean check = false;

    // game with players - intended for play
    public Game(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.board = new Board();
        this.board.setGame(this);
    }

    // game with no players - used for analysis
    public Game() {
        this.depth = 0;
        this.board = new Board();
        this.board.setGame(this);
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setCastlingRights("KQkq");
        this.setEnPassantTarget("-");
        this.setHalfmoveClock(0);
        this.setFullmoveCounter(1);
        this.generateMoves();
    }

    public Game(String fenString) {
        this(fenString, 0);
    }
    // game that can start midway through - used for analysis
    public Game(String fenString, int depth) {
        FenString fen = new FenString(fenString);
        this.depth = depth;
        this.board = new Board(fen.getPiecePlacement());
        this.board.setGame(this);
        this.setActivePlayerColor(fen.getActivePlayerColor());
        this.setCastlingRights(fen.getCastlingRights());
        this.setEnPassantTarget(fen.getEnPassantTarget());
        this.setHalfmoveClock(fen.getHalfmoveClock());
        this.setFullmoveCounter(fen.getFullmoveCounter());

        // Are we in check?
        this.generateMoves();
        if(isChecked(activePlayerColor.otherColor()))
            setPlayerInCheck();
    }

    public void setDepth(int depth) { this.depth = depth; }

    public String getPiecePlacement() {
        return this.getBoard().toFen();
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

    public PlayerColor getWaitingPlayer() {
        return this.activePlayerColor.otherColor();
    }

    /*
     * iterate over all pieces on the board
     * for each piece, generate the moves for that piece
     * store the moves in a list
     * return number of moves
     * 1 ply
     *
     * generateMoves() can be thought of as a method for the client to use.  The client is the thing that understands
     * valid moves.  The server enforces that the move is technically correct (i.e. a pawn can move 2 paces from the
     * start position) but not fancy rules like en passant and castling.  Is this right???
     *
     * This method should not be called as part of the "move" method as it would be too expensive.
     */
    public void generateMoves() {
        // reset the current state
        this.activePlayerMoves = new MoveList<Move>(new ArrayList<Move>());
        this.otherPlayerMoves = new MoveList<Move>(new ArrayList<Move>());

        MoveList<Move> list;

        // TODO find a faster way to iterate over the board.
        for (int i = 0; i < 64; i++) {
            Piece piece = this.getBoard().get(i);

            if (piece == Piece.EMPTY) // || (onlyActivePlayer && this.activePlayerColor != piece.getColor()))
                continue;

            PieceMoves rawMoves = piece.generateMoves(this.board, i, getCastlingRights(), getEnPassantTargetAsInt());
            list = piece.getColor() == getActivePlayerColor() ? this.activePlayerMoves : this.otherPlayerMoves;

            for(int dest: Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                list.add(new Move(piece, i, dest, MoveFlag.NONCAPTURE));
            }

            for(int dest: Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                list.add(new Move(piece, i, dest, MoveFlag.CAPTURE));
            }
        }

//        LOGGER.debug("pieces with generated moves: " + this.getNextMoves().size());
        ensureMovesGetsPlayerOutOfCheck(this.activePlayerMoves);
    }

    public MoveList<Move> getActivePlayerMoves() { return this.activePlayerMoves; };
    public MoveList<Move> getOtherPlayerMoves() { return this.otherPlayerMoves; };

    private void ensureMovesGetsPlayerOutOfCheck(MoveList<Move> moves) {
        if(depth > 0)
            return;

        // if in check, make sure any move takes the player out of check.
        MoveList<Move> toRemove = new MoveList<Move>(new ArrayList<Move>());

        for(var move: moves) {
            Game tempGame = new Game(this.asFen(), depth + 1);
            tempGame.move(move);

            // if the player remains in check after the move, then it is not valid.  Remove it from the moves list.
            if(tempGame.isChecked(this.activePlayerColor)) {
                toRemove.add(move);
            }
        }

        moves.removeAll(toRemove);
    }

    // using chess algebraic notation
    // this method executes most moves sent to it.  It isn't checking for correctness.
    public void move(String action) {
        int[] locations;
        // TODO validate action

        // TODO validate move

        // test if the action is valid
        switch (action.charAt(0)) {
            case 'R', 'N', 'B', 'Q', 'K' -> {
                findSourceAndDestination(action);
                this.getBoard().move(this.sourceLocation, this.destinationLocation);
            }
            case 'O' -> {
                // TODO: How do I use the knowledge in KingMoves when castling?
                findSourceAndDestination(action);
                this.getBoard().move(this.sourceLocation, this.destinationLocation);
                this.getBoard().move(this.secondarySourceLocation, this.secondaryDestinationLocation);
            }
            default -> {
                // can this be moved into the findSourceAndDestination method?
                // pawn move
                int destination = FenString.squareToLocation(action);
                locations = this.getBoard().getPawnLocations(this.getActivePlayerColor());
                boolean moved = false;

                // TODO: this can be done smarter. if we know the destination (from the action),
                // then
                // we don't need to iterate through all the locations and instead we can take
                // the pawnlocatin that
                // is in the same file as the destination.
                for (int i : locations) {
                    Piece piece = this.getBoard().get(i);
                    PieceMoves bm = piece.generateMoves(this.board, i, getCastlingRights(), getEnPassantTargetAsInt());

                    moved = movePawn(i, bm, destination);

                    // update the en passant value
                    updateForEnPassant(i, moved, destination);
                }
            }
        }

        postMoveAccounting();
    }

    public void move(Move move) {
        setPlayerNotInCheck();
        this.sourceLocation = move.getFrom();
        this.destinationLocation = move.getTo();
        this.getBoard().move(this.sourceLocation, this.destinationLocation);

        postMoveAccounting();
    }

    private void postMoveAccounting() {
        removeCastlingRightsFor(this.sourceLocation);
        switchActivePlayer();
        generateMoves();

        // If we put the other play in check, then record it.
        if(isChecked(this.activePlayerColor))
            setPlayerInCheck();

        incrementClock();
    }

    private void setPlayerNotInCheck() {
        this.check = false;
    }

    private void setPlayerInCheck() {
        this.check = true;
    }

    private void incrementClock() {
        if(halfmoveClock == 0)
            halfmoveClock = 1;
        else {
            halfmoveClock = 0;
            fullmoveCounter++;
        }
    }

    private void switchActivePlayer() {
        this.activePlayerColor = this.activePlayerColor.otherColor();
/*
        if (this.activePlayerColor == PlayerColor.WHITE) {
            this.activePlayerColor = PlayerColor.BLACK;
        } else {
            this.activePlayerColor = PlayerColor.WHITE;
        }
*/
    }

    private boolean movePawn(int i, PieceMoves bm, int destination) {
        boolean moved;
        if ((bm.getNonCaptureMoves() & (1L << destination)) != 0) {
            this.getBoard().move(i, destination);
            moved = true;
        } else if ((bm.getCaptureMoves() & (1L << destination)) != 0) {
            // normal capture
            this.getBoard().move(i, destination);
            moved = true;
        } else {
            moved = false;
        }
        return moved;
    }

    private void updateForEnPassant(int location, boolean moved, int destination) {
        if (moved) {
            // if the pawn is on their home position && if the destination is two moves away
            if (location >= 8 && location <= 15 && destination - location == 16) { // two moves away

                if (destination < 31 &&
                        (((1L << (destination + 1)) & this.getBoard().getBlackPawns()) != 0))
                    this.setEnPassantTarget(FenString.locationToSquare(location + 8));

                if (destination > 24 &&
                        (((1L << (destination - 1)) & this.getBoard().getBlackPawns()) != 0))
                    this.setEnPassantTarget(FenString.locationToSquare(location + 8));

            } else if (location >= 48 && location <= 55 && destination - location == -16) {

                // set the en passant target
                if (destination < 39 &&
                        (((1L << (destination + 1)) & this.getBoard().getWhitePawns()) != 0))
                    this.setEnPassantTarget(FenString.locationToSquare(location - 8));

                if (destination > 32 &&
                        (((1L << (destination - 1)) & this.getBoard().getWhitePawns()) != 0))
                    this.setEnPassantTarget(FenString.locationToSquare(location - 8));

            } else {
                // reset the en passant target
                this.setEnPassantTarget(PawnMoves.NO_EN_PASSANT);
            }
        }
    }

    /*
     * This method simply finds the source and target positions.  It doesn't check if the
     * for any conditions such as whether the king is in check.
     *
     * Examples:
     * d2    - pawns may have no char to represent them
     * Nd4   - non pawns will have their type as the first character
     * Nxb8  - captures will have an x
     * cxb5  - pawn captures will start with the file
     * Nxc8+ - check
     * Nxc8* - checkmate
     * O-O   - king side castle
     * O-O-O - queen side castle
     * e8=Q  - pawn promotion
     *
     * need to work backward from the destination to the source.
     * 1. the piece type can be derived from the action (first char)
     * 2. source has to be derived from the destination
     */
    private void findSourceAndDestination(String action) {
        /*
         when there is ambiguity
         [https://www.chessprogramming.org/Algebraic_Chess_Notation#Ambiguities], use
         the following rules in order:
         1. file of departure if different
         2. rank of departure if the files are the same but the ranks differ
         3. the complete origin square coordinate otherwise
        */
        char[] actionChars = action.toCharArray();

        // source and destination for castling represents the kings movement
        if (action.equals("O-O-O")) {
            if (this.getActivePlayerColor() == PlayerColor.BLACK) {
                this.sourceLocation = CastlingLocations.BLACK_INITIAL_KING_LOCATION.location();
                this.destinationLocation = CastlingLocations.BLACK_QUEENS_SIDE_CASTLING_KING_LOCATION.location();
                this.secondarySourceLocation = CastlingLocations.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION.location();
                this.secondaryDestinationLocation = CastlingLocations.BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION.location();

                return;
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                this.sourceLocation = CastlingLocations.WHITE_INITIAL_KING_LOCATION.location();
                this.destinationLocation = CastlingLocations.WHITE_QUEENS_SIDE_CASTLING_KING_LOCATION.location();
                this.secondarySourceLocation = CastlingLocations.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION.location();
                this.secondaryDestinationLocation = CastlingLocations.WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION.location();

                return;
            }
        } else if (action.equals("O-O")) {
            // king side castle
            if (this.getActivePlayerColor() == PlayerColor.BLACK) {
                this.sourceLocation = CastlingLocations.BLACK_INITIAL_KING_LOCATION.location();
                this.destinationLocation = CastlingLocations.BLACK_KING_SIDE_CASTLING_KING_LOCATION.location();
                this.secondarySourceLocation = CastlingLocations.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION.location();
                this.secondaryDestinationLocation = CastlingLocations.BLACK_KING_SIDE_CASTLING_ROOK_LOCATION.location();

                return;
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                this.sourceLocation = CastlingLocations.WHITE_INITIAL_KING_LOCATION.location();
                this.destinationLocation = CastlingLocations.WHITE_KING_SIDE_CASTLING_KING_LOCATION.location();
                this.secondarySourceLocation = CastlingLocations.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION.location();
                this.secondaryDestinationLocation = CastlingLocations.WHITE_KING_SIDE_CASTLING_ROOK_LOCATION.location();

                return;
            }
        }

        int destination = FenString.squareToLocation(action);
        int source = -1;
        int[] possibleSourceLocations;

        switch (actionChars[0]) {
            case 'N' -> possibleSourceLocations = this.getBoard().getKnightLocations(this.getActivePlayerColor());
            case 'B' -> possibleSourceLocations = this.getBoard().getBishopLocations(this.getActivePlayerColor());
            case 'R' -> possibleSourceLocations = this.getBoard().getRookLocations(this.getActivePlayerColor());
            case 'Q' -> possibleSourceLocations = this.getBoard().getQueenLocations(this.getActivePlayerColor());
            case 'K' -> possibleSourceLocations = this.getBoard().getKingLocations(this.getActivePlayerColor());
            default -> possibleSourceLocations = new int[0];
        }

        char rankSpecified = 0;
        char fileSpecified = 0;

        // If this is a capture move (the 'x') then work out whether we should prioritize the file or rank.
        if (action.length() == 5 && action.charAt(2) == 'x') {
            if (action.charAt(1) >= 'a' && action.charAt(1) <= 'h')
                fileSpecified = action.charAt(1);
            else if (action.charAt(1) >= '1' && action.charAt(1) <= '8')
                rankSpecified = action.charAt(1);
        }

        int tempLocation;
        boolean capture = false;

        for (int i = 0; i < possibleSourceLocations.length && source == -1; i++) {
            tempLocation = possibleSourceLocations[i];
            Piece piece = this.getBoard().get(tempLocation);
            PieceMoves pm = piece.generateMoves(this.board, tempLocation, getCastlingRights(),
                    getEnPassantTargetAsInt());

            // playing non-capture move
            if ((pm.getNonCaptureMoves() & (1L << destination)) != 0) {
                source = tempLocation;
            } else if ((pm.getCaptureMoves() & (1L << destination)) != 0) {
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

    /*
     * If the King or their respective rooks move, then remove the castling option
     */
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

        if(this.castlingRights.isEmpty())
            this.castlingRights = "-";
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
    
    public Board getBoard() {
        return this.board;
    }

    public boolean isOver() {
        return isCheckmated(this.activePlayerColor) || hasResigned() || isStalemate() || hasInsufficientMaterials() || exceededMoves() || hasRepeated();
    }

    public boolean exceededMoves() {
        return limitMovesTo50 && past50moves();
    }

    public boolean hasRepeated() {
        return false;
    }

    public boolean past50moves() {
        return fullmoveCounter > 50;
    }

    public boolean hasInsufficientMaterials() {
        return getBoard().hasInsufficientMaterials(isOutOfTime());
    }

    public void markTimeComplete() {
        outOfTime = true;
    }

    public boolean isOutOfTime() {
        return outOfTime;
    }

    public boolean isStalemate() {
        return false;
    }

    public boolean hasResigned() {
        return false;
    }

    public boolean isChecked(PlayerColor playerColor) {
        int kingLocation = getBoard().getKingLocation(playerColor);
        var moves = this.activePlayerColor == playerColor ? this.otherPlayerMoves : this.activePlayerMoves;

        for(Move move : moves) {
            // skip this move if it isn't trying to capture the king.
            if(move.getFlags() == MoveFlag.NONCAPTURE || move.getTo() != kingLocation)
                continue;

            return true;
        }

        return false;
    }

    public boolean isCheckmated(PlayerColor playerColor) {
        return this.activePlayerMoves.isEmpty();
    }

    public void disable50MovesRule() {
        this.limitMovesTo50 = false;
    }

    public int perft(String fen, int depth) {
        if(depth == 0)
            return 0;

        Game game = new Game(fen);
        game.generateMoves();
        MoveList<Move> moves = game.getActivePlayerMoves();
        int moveCounter = 0;
        String newFen;

        for(var move: moves) {
            Game temp = new Game(game.asFen());
            temp.move(move);
            newFen = temp.asFen();
            temp = null;
            if(depth - 1 > 0 )  moveCounter += perft(newFen, depth - 1);
            else moveCounter++;
        }

        return moveCounter;
    }

}
