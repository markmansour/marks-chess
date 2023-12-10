package com.stateofflux.chess.model;

import java.util.*;

import com.stateofflux.chess.model.pieces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Only legal moves can happen through the Game object.  It acts as the
 * validation layer of the Board object.
 */
public class Game {
    // TODO replace most integers with bytes to save space
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;
    protected MoveList<Move> activePlayerMoves;
    protected MoveList<Move> otherPlayerMoves;
    protected MoveList<Move> moveHistory;

    protected int enPassantTarget;
    private boolean check = false;
    private boolean limitMovesTo50 = true;
    private int depth = 0;
    private final LinkedList<Long> history = new LinkedList<>();

    // If neither side has the ability to castle, this field uses the character "-".
    // Otherwise, this field contains one or more letters: "K" if White can castle
    // kingside, "Q" if White can castle queenside, "k" if Black can castle
    // kingside, and "q" if Black can castle queenside. A situation that temporarily
    // prevents castling does not prevent the use of this notation.
    protected String castlingRights;


    // The number of halfmoves since the last capture or pawn advance, used for the
    // fifty-move rule
    protected int halfmoveClock;

    // The number of the full moves. It starts at 1 and is incremented after Black's
    // move
    protected int fullmoveCounter;
    private boolean outOfTime = false;

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

    public Game(Game game) {
        this(game, 0);
    }

    public Game(Game game, int depth) {
        this.depth = depth;
        this.board = new Board(game.getPiecePlacement());
        this.board.setGame(this);

        this.setActivePlayerColor(game.getActivePlayerColor());
        this.setCastlingRights(game.getCastlingRights());
        this.setEnPassantTarget(game.getEnPassantTarget());
        this.setHalfmoveClock(game.getHalfmoveClock());
        this.setFullmoveCounter(game.getFullmoveCounter());

        // Are we in check?
        this.generateMoves();
        if (isChecked(activePlayerColor.otherColor()))
            setPlayerInCheck();
        else
            setPlayerNotInCheck();
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
        this.setHalfmoveClock(fen.getHalfmoveClock());      // TODO: This is not right
        this.setFullmoveCounter(fen.getFullmoveCounter());  // TODO: THis is not right.  We don't know how many moves have been taken.

        this.generateMoves();

        if(isChecked(activePlayerColor.otherColor()))
            setPlayerInCheck();
        else
            setPlayerNotInCheck();
    }

    public Game(Pgn pgn) {
        this.depth = 0;
        this.board = new Board();
        this.board.setGame(this);
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setCastlingRights("KQkq");
        this.setEnPassantTarget("-");
        this.setHalfmoveClock(0);
        this.setFullmoveCounter(1);
        this.generateMoves();

        for(var move : pgn.getMoves()) {
//            LOGGER.info(move.toString());
            this.move(move.whiteMove());
            if(! move.blackMove().isBlank())
                this.move(move.blackMove());
//            this.getBoard().printOccupied();
        }
    }

    static public Game fromSan(String san) {
        Game g = new Game();
        List<PgnMove> pgnMoves = Pgn.fromSan(san);

        for(var pgnMove : pgnMoves) {
            g.move(pgnMove.whiteMove());

            if(! pgnMove.blackMove().isBlank()) {
                g.move(pgnMove.blackMove());
            }

            g.getBoard().printOccupied();
        }

        return g;
    }

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
        char pieceChar;

        // reset the current state
        this.activePlayerMoves = new MoveList<Move>(new ArrayList<Move>());
        this.otherPlayerMoves = new MoveList<Move>(new ArrayList<Move>());

        MoveList<Move> list;
        Move m;

        // TODO find a faster way to iterate over the board.
        for (int i = 0; i < 64; i++) {
            Piece piece = this.getBoard().get(i);

            if (piece == Piece.EMPTY) // || (onlyActivePlayer && this.activePlayerColor != piece.getColor()))
                continue;

            PieceMoves rawMoves = piece.generateMoves(this.board, i, getCastlingRights(), getEnPassantTargetAsInt());
            pieceChar = piece.getPieceChar();
            list = piece.getColor() == getActivePlayerColor() ? this.activePlayerMoves : this.otherPlayerMoves;

            // TODO: There has to be a better way to do this.
            for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                if (pieceChar == Piece.WHITE_PAWN.getPieceChar() && dest / 8 == 7) {  // white is promoting
                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.WHITE_QUEEN);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.WHITE_BISHOP);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.WHITE_KNIGHT);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.WHITE_ROOK);
                    list.add(m);

                } else if (pieceChar == Piece.BLACK_PAWN.getPieceChar() && dest / 8 == 0) {  // black is promoting
                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.BLACK_QUEEN);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.BLACK_BISHOP);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.BLACK_KNIGHT);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.setPromotion(Piece.BLACK_ROOK);
                    list.add(m);
                } else { // normal move
                    m = new Move(piece, i, dest, Move.NON_CAPTURE);
                    m.updateMoveForCastling(check, castlingRights);
                    m.updateForEnPassant(getBoard().getWhitePawns(), getBoard().getBlackPawns());
                    list.add(m);
                }
            }

            for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                if (pieceChar == Piece.WHITE_PAWN.getPieceChar() && dest / 8 == 7) {  // white is promoting
                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.WHITE_QUEEN);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.WHITE_BISHOP);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.WHITE_KNIGHT);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.WHITE_ROOK);
                    list.add(m);

                } else if (pieceChar == Piece.BLACK_PAWN.getPieceChar() && dest / 8 == 0) {  // black is promoting
                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.BLACK_QUEEN);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.BLACK_BISHOP);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.BLACK_KNIGHT);
                    list.add(m);

                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.setPromotion(Piece.BLACK_ROOK);
                    list.add(m);
                } else { // normal move
                    m = new Move(piece, i, dest, Move.CAPTURE);
                    m.updateMoveForCastling(check, castlingRights);
                    m.updateForEnPassant(getBoard().getWhitePawns(), getBoard().getBlackPawns());
                    list.add(m);
                }
            }
        }

//        LOGGER.debug("pieces with generated moves: " + this.getNextMoves().size());
        ensureMovesGetsPlayerOutOfCheck(this.activePlayerMoves);
    }

    public MoveList<Move> getActivePlayerMoves() {
        return this.activePlayerMoves;
    }


    private void ensureMovesGetsPlayerOutOfCheck(MoveList<Move> moves) {
        if (depth > 0)
            return;

        // if in check, make sure any move takes the player out of check.
        MoveList<Move> toRemove = new MoveList<Move>(new ArrayList<Move>());

        for (var move : moves) {
            Game tempGame = new Game(this, depth + 1);

            // can't use castling to get out of check
            if(tempGame.isChecked(this.activePlayerColor) && move.isCastling()) {
                toRemove.add(move);
                continue;
            }

            tempGame.move(move);

            // if the player remains in check after the move, then it is not valid.  Remove it from the moves list.
            if (tempGame.isChecked(this.activePlayerColor)) {
                toRemove.add(move);
            }
        }

        moves.removeAll(toRemove);
    }

    /*
     * Long Alegbraic notation without piece names is in the format
     * <FROM_AS_SQUARE_COORDS><TO_AS_SQUARE_COORDS><PROMOTION>
     * e.g.
     * - c2c3  <-- move pawn one space
     * - b7b8Q <-- queen promotion
     */
    public void moveLongNotation(String action) {
        String from, to;
        from = action.substring(0,2);
        to = action.substring(2, 4);
        Piece promotion = (action.length() == 5) ? Piece.getPieceByPieceChar(action.substring(4,5)) : null;

        int fromSquare = FenString.squareToLocation(from);
        int toSquare = FenString.squareToLocation(to);
        Piece piece = board.get(fromSquare);

        Move m = new Move(piece, fromSquare, toSquare, false);

        if((piece == Piece.WHITE_PAWN && toSquare >= 56) || (piece == Piece.BLACK_PAWN && toSquare <= 7))
            m.setPromotion(promotion);
        else {
            m.updateMoveForCastling(check, castlingRights);
            m.updateForEnPassant(getBoard().getWhitePawns(), getBoard().getBlackPawns());
        }

        move(m);
    }

    // using chess algebraic notation
    // this method executes most moves sent to it.  It isn't checking for correctness.
    // TODO: Instead of setting sourceLocation and destinationLocation, make an object to hold the move data.  Why am
    // I not using the exiting Move class?
    /*
     * This method simply finds the source and target positions.  It doesn't check if the
     * for any conditions such as whether the king is in check.
     *
     * Examples:
     * d3 - implied pawn + destination
     * dxe3 - implied pawn + rank + destination
     * d3xe3 im implied pawn + rank + file + capture + destination
     * d3+ - implied pawn + destination + check
     * Nxc8* - checkmate
     * Nd3 - piece + destination
     * Ncd3 - piece + rank + destination
     * N5d3 - piece + file + destination
     * Nc5d3 - piece + rank + file + destination
     * O-O   - king side castle
     * O-O-O - queen side castle
     * e8=Q  - pawn promotion
     * e8=Q+  - pawn promotion and check
     *
     * need to work backward from the destination to the source.
     * 1. the piece type can be derived from the action (first char)
     * 2. source has to be derived from the destination
     *
     * when there is ambiguity
     *    [https://www.chessprogramming.org/Algebraic_Chess_Notation#Ambiguities], use
     *    the following rules in order:
     *    1. file of departure if different
     *    2. rank of departure if the files are the same but the ranks differ
     *    3. the complete origin square coordinate otherwise
     *
     */
    public void move(String action) {
        LOGGER.info("action ({}): {}", getActivePlayerColor(), action);
        Piece promotionPiece = null;
        Move move;

        // Castling
        move = extractCastlingMove(action);

        if (move == null) {
            // Promotion
            int promotionMarker = action.indexOf('=');
            if (promotionMarker >= 0) {
                promotionPiece = Piece.getPieceByPieceChar(action.substring(promotionMarker + 1, promotionMarker + 2));
                action = action.substring(0, promotionMarker);
            }

            move = extractMoveFromAlgebraicNotation(action);
            move.setPromotion(promotionPiece);  // ok to set to null
            move.updateForEnPassant(getBoard().getWhitePawns(), getBoard().getBlackPawns());
            setEnPassantTarget(move.getEnPassantTarget());
        }

        int removedLocation = this.getBoard().update(move);
        postMoveAccounting(move, removedLocation);
    }

    @NotNull
    private Move extractMoveFromAlgebraicNotation(String action) {
        int destination = FenString.squareToLocation(action);
        int source = -1;
        int[] possibleSourceLocations;

        int takeMarker = action.contains("x") ? 1 : 0;
        String sourceHint = switch (action.charAt(action.length() - 1)) {
            case '+', '*', '#' -> action.substring(0, action.length() - 3 - takeMarker);
            default -> action.substring(0, action.length() - 2 - takeMarker);
        };

        char pieceToMoveChar = sourceHint.isEmpty() ? 0 : sourceHint.charAt(0);
        String sourceHintWithoutPiece;

        switch(pieceToMoveChar) {
            case 'N', 'B', 'R', 'Q', 'K' -> sourceHintWithoutPiece = sourceHint.substring(1);
            default -> sourceHintWithoutPiece = sourceHint;
        }

        char rankSpecified = 0;
        char fileSpecified = 0;
        char firstChar;

        switch(sourceHintWithoutPiece.length()) {
            case 1:
                firstChar = sourceHintWithoutPiece.charAt(0);
                if(firstChar >= '1' && firstChar <= '8')
                    rankSpecified = firstChar;
                else if (firstChar >= 'a' && firstChar <= 'h') {
                    fileSpecified = firstChar;
                }
                break;
            case 2:
                rankSpecified = sourceHintWithoutPiece.charAt(0);
                fileSpecified = sourceHintWithoutPiece.charAt(1);
                break;
        }

        int tempLocation;
        boolean capture = false;

        possibleSourceLocations =
            switch(pieceToMoveChar) {
                case Piece.KING_ALEGBRAIC -> this.getBoard().getKingLocations(this.getActivePlayerColor());
                case Piece.QUEEN_ALEGBRAIC -> this.getBoard().getQueenLocations(this.getActivePlayerColor());
                case Piece.BISHOP_ALEGBRAIC -> this.getBoard().getBishopLocations(this.getActivePlayerColor());
                case Piece.KNIGHT_ALEGBRAIC -> this.getBoard().getKnightLocations(this.getActivePlayerColor());
                case Piece.ROOK_ALEGBRAIC -> this.getBoard().getRookLocations(this.getActivePlayerColor());
                default -> this.getBoard().getPawnLocations(this.getActivePlayerColor());
            };

        Piece piece = null;

        for (int possibleSourceLocation : possibleSourceLocations) {
            tempLocation = possibleSourceLocation;
            piece = this.getBoard().get(tempLocation);
            PieceMoves pm = piece.generateMoves(this.board, tempLocation, getCastlingRights(), getEnPassantTargetAsInt());

            // if the piece being reviewed isn't in the rank specified then skip over it
            if (rankSpecified != 0 && (('1' + (tempLocation / 8)) != rankSpecified)) {
                continue;
            }

            // if the piece being reviewed isn't in the file specified then skip over it
            if (fileSpecified != 0 && (('a' + (tempLocation % 8)) != fileSpecified)) {
                continue;
            }

            if ((pm.getNonCaptureMoves() & (1L << destination)) != 0) {
                source = tempLocation;
                break;
            }

            if ((pm.getCaptureMoves() & (1L << destination)) != 0) {
                capture = true;
                source = tempLocation;
                break;
            }
        }

        assert(source != -1);  // source wasn't found in the possible locations

        return new Move(piece, source, destination, capture);
    }

    /*
     * expects en passant, castling, and promotion to be set in the move object.
     */
    public void move(Move move) {
        int removed = this.getBoard().update(move);
        postMoveAccounting(move, removed);
    }

    public LinkedList<Long> getHistory() {
        return history;
    }

    private void postMoveAccounting(Move move, int removedLocation) {
        removeCastlingRightsFor(move.getFrom(), removedLocation);

        history.add(getZobristKey());

        switchActivePlayer();
        generateMoves();
        removeEnPassantIfAttackingPieceIsPinned();

        // If we put the other play in check, then record it.
        if (isChecked(this.activePlayerColor))
            setPlayerInCheck();
        else
            setPlayerNotInCheck();

        incrementClock();
    }

    private void removeEnPassantIfAttackingPieceIsPinned() {
        // get each move by a Pawn that can end up in the en passant position
        var moves = getActivePlayerMoves().stream()
            .filter(m -> m.getPiece().isPawn())
            .filter(m -> m.getTo() == getEnPassantTargetAsInt())
            .toArray();

        // if there are no valid attacking pawn moves for en passant, then remove the en passant and recaluclate the
        // zobrist hash.
        if (moves.length == 0) { // the pawns cannot use en passant to take the initial 2 square move
            history.removeLast();
            setEnPassantTarget(PawnMoves.NO_EN_PASSANT);
            history.add(getZobristKey());
        }
    }

    private void setPlayerNotInCheck() {
        this.check = false;
    }

    private void setPlayerInCheck() {
        this.check = true;
    }

    private void incrementClock() {
        if (halfmoveClock == 0)
            halfmoveClock = 1;
        else {
            halfmoveClock = 0;
            fullmoveCounter++;
        }
    }

    private void switchActivePlayer() {
        this.activePlayerColor = this.activePlayerColor.otherColor();
    }

    private Move extractCastlingMove(String action) {
        Piece castlingPiece = null;
        int sourceLocation = -1;
        int destinationLocation = -1;
        int secondarySourceLocation = -1;
        int secondaryDestinationLocation = -1;
        Move move;

        if (action.equals("O-O-O")) {  // queen side castling
            if (this.getActivePlayerColor() == PlayerColor.BLACK) {
                castlingPiece = Piece.BLACK_KING;
                sourceLocation = CastlingLocations.BLACK_INITIAL_KING_LOCATION.location();
                destinationLocation = CastlingLocations.BLACK_QUEENS_SIDE_CASTLING_KING_LOCATION.location();
                secondarySourceLocation = CastlingLocations.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION.location();
                secondaryDestinationLocation = CastlingLocations.BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION.location();
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                castlingPiece = Piece.WHITE_KING;
                sourceLocation = CastlingLocations.WHITE_INITIAL_KING_LOCATION.location();
                destinationLocation = CastlingLocations.WHITE_QUEENS_SIDE_CASTLING_KING_LOCATION.location();
                secondarySourceLocation = CastlingLocations.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION.location();
                secondaryDestinationLocation = CastlingLocations.WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION.location();
            }
        } else if (action.equals("O-O")) { // king side castle
            if (this.getActivePlayerColor() == PlayerColor.BLACK) {
                castlingPiece = Piece.BLACK_KING;
                sourceLocation = CastlingLocations.BLACK_INITIAL_KING_LOCATION.location();
                destinationLocation = CastlingLocations.BLACK_KING_SIDE_CASTLING_KING_LOCATION.location();
                secondarySourceLocation = CastlingLocations.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION.location();
                secondaryDestinationLocation = CastlingLocations.BLACK_KING_SIDE_CASTLING_ROOK_LOCATION.location();
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                castlingPiece = Piece.WHITE_KING;
                sourceLocation = CastlingLocations.WHITE_INITIAL_KING_LOCATION.location();
                destinationLocation = CastlingLocations.WHITE_KING_SIDE_CASTLING_KING_LOCATION.location();
                secondarySourceLocation = CastlingLocations.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION.location();
                secondaryDestinationLocation = CastlingLocations.WHITE_KING_SIDE_CASTLING_ROOK_LOCATION.location();
            }
        }

        if(castlingPiece != null) {
            move = new Move(castlingPiece, sourceLocation, destinationLocation, Move.NON_CAPTURE);
            move.setCastling(secondarySourceLocation, secondaryDestinationLocation);

            return move;
        }

        return null;
    }

    /*
     * If the King or their respective rooks move, then remove the castling option
     */
    private void removeCastlingRightsFor(int i, int dest) {
        removeCastlingRightsForLocation(i);
        removeCastlingRightsForLocation(dest);

        if (this.castlingRights.isEmpty())
            this.castlingRights = "-";
    }

    private void removeCastlingRightsForLocation(int location) {
        this.castlingRights = switch (location) {
            case 0 -> this.castlingRights.replace("Q", "");
            case 4 -> this.castlingRights.replace("K", "").replace("Q", "");
            case 7 -> this.castlingRights.replace("K", "");
            case 56 -> this.castlingRights.replace("q", "");
            case 60 -> this.castlingRights.replace("k", "").replace("q", "");
            case 63 -> this.castlingRights.replace("k", "");
            default -> this.castlingRights;
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
    
    public Board getBoard() {
        return this.board;
    }

    public boolean isOver() {
        return isCheckmated(this.activePlayerColor) || hasResigned() || isStalemate() || hasInsufficientMaterials() || exceededMoves() || isRepetition();
    }

    public boolean exceededMoves() {
        return limitMovesTo50 && past50moves();
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
        return isChecked(this.getActivePlayerColor()) && this.getActivePlayerMoves().isEmpty();
    }

    public boolean hasResigned() {
        return false;
    }

    public boolean isChecked(PlayerColor playerColor) {
        int kingLocation = getBoard().getKingLocation(playerColor);
        var moves = this.activePlayerColor == playerColor ? this.otherPlayerMoves : this.activePlayerMoves;

        for (Move move : moves) {
            // skip this move if it isn't trying to capture the king.
            if (!move.isCapture() || move.getTo() != kingLocation)
                continue;

            return true;
        }

        return false;
    }

    public boolean isDraw() {
        return getFullmoveCounter() * 50 >= 100 ||
            isStalemate() ||
            hasInsufficientMaterials() ||
            isRepetition();
    }

    public boolean isCheckmated(PlayerColor playerColor) {
        return this.activePlayerMoves.isEmpty();
    }

    public void disable50MovesRule() {
        this.limitMovesTo50 = false;
    }

    public SortedMap<String, Integer> perftAtRoot(Game oldGame, int depth) {
        SortedMap<String, Integer> perftResults = new TreeMap<>();

        Game game = new Game(oldGame);
        game.generateMoves();
        MoveList<Move> moves = game.getActivePlayerMoves();
        int moveCounter = 0;

        for (var move : moves) {
            Game temp = new Game(game);
            // temp.moveAsSan(move.toLongSan());
            temp.move(move);
            moveCounter = perft(temp, depth - 1);
            perftResults.put(move.toLongSan(), moveCounter);
        }

        printPerft(perftResults);

        return perftResults;
    }

    public void printPerft(SortedMap<String, Integer> perftResults) {
        LOGGER.info("Perft Results");
        LOGGER.info("-------------");
        LOGGER.info("Game {}", this.asFen());
        for(var r : perftResults.keySet()) {
            LOGGER.info("{} {}", r, perftResults.get(r));
        }
    }

    public int perft(Game oldGame, int depth) {
        if (depth == 0)
            return 1;

        Game game = new Game(oldGame);
        game.generateMoves();
        MoveList<Move> moves = game.getActivePlayerMoves();
        int moveCounter = 0;

        for (var move : moves) {
            Game temp = new Game(game);
            temp.move(move);
            moveCounter += perft(temp, depth - 1);
        }

        return moveCounter;
    }

    /*
     * https://web.archive.org/web/20070810003508/www.seanet.com/%7Ebrucemo/topics/zobrist.htm
        # A means of enabling position comparison

        A chess position is comprised of the pieces on the board, side to move, castling legality, and en-passant
        capture legality.

        When writing a chess program, it is necessary to be able to compare two positions in order to see if they are
        the same.  If you had to compare the position of each piece, it wouldn't take long to do, but in practice you
        will have to do this thousands of times per second, so if you did it this way it could become a performance
        bottleneck.  Also, the number of positions that you will save for future comparison is extremely large, so
        storing the location of each piece, etc., ends up taking too much space.

        A solution to this problem involves creation of a signature value, typically 64-bits.  Since 64 bits is not
        enough to enumerate every chess position, there will exist the possibility of a signature collision, but in
        practice this is rare enough that it can be ignored, as long as you make sure you don't actually crash if you
        get a collision.

        Whether or not 32-bits is enough is open to some debate, but conventional wisdom says "no".

        A popular way of implementing signatures involves Zobrist keys.

        # Implementation

        You start by making a multi-dimensional array of 64-bit elements, each of which contains a random number.  In
        C, the "rand()" function returns a 15-bit value (0..32767), so in order to get a 64-bit random number you will
        probably have to do a little work.  Actually, I'll do it for you.  Here is a 64-bit random number function:

        U64 rand64(void)
        {
            return rand() ^ ((U64)rand() << 15) ^ ((U64)rand() << 30) ^
                ((U64)rand() << 45) ^ ((U64)rand() << 60);
        }

        This function works by filling up a whole "U64" element (you define this element yourself, depending upon what
        compiler you use -- try "long long" or "__int64") with hunks of gibberish returned by "rand()".

        Anyway, your table has three dimensions -- type of piece, color of piece, and location of piece:

        U64 zobrist[pcMAX][coMAX][sqMAX];
        You start by filling this up with random numbers when the program boots.  The numbers need to be pretty random.
        I've read Usenet posts and so on where people claim that "rand()" is not random enough for this, but "rand()"
        is what I've always used and I've never had a problem.  If you want to make something more random, more power
        to you, but make sure that you don't use anything less random than "rand()".

        To create a zobrist key for a position, you set your key to zero, then you go find every piece on the board,
        and XOR (via the "^" operator) "zobrist[pc][co][sq]" into your key.

        If the position is white to move, you leave it alone, but if it's black to move, you XOR a 64-bit constant
        random number into the key.

        # Why Zobrist keys are especially nice

        This Zobrist key technique creates keys that aren't related to the position being keyed.  If a single piece or
        pawn is moved, you get a key that's completely different, so these keys don't tend to clump up or collide very
        much.  This is good if you are trying to use them as hash keys.

        Another nice thing is that you can manage these keys incrementally.  If, for example, you have a white pawn on
        e5, the key has had "zobrist[PAWN][WHITE][E5]" XOR'd into it.  If you XOR this value into the key again, due
        to the way that XOR works, the pawn is deleted from the key.

        What this means is that if you have a key for the current position, and want to move a white pawn from e5 to
        e6, you XOR in the "white pawn on e5" key, which removes the pawn from e5, and XOR in the "white pawn on e6"
        key, which puts a white pawn on e6.  You are guaranteed to get the same key that you'd get if you started over
        and XOR'd all of the keys for all of the pieces together.

        If you want to switch side to move, you XOR the "change side to move" key in.  You can also manage castling
        and en-passant values the same way.

        The utility of this is that you can create a zobrist key at the root of the search, and keep it current
        throughout the course of the search by updating it in "MakeMove()".

        # Some uses for these keys

        * These Zobrist keys are often used as hash keys.  Hash keys have several uses in chess programs:
        * You can use them to implement a transposition table.  This is a large hash table that allows you to keep track
          of positions that you've seen during the search.  This will let you save work in some cases.  If you are going
          to search a position to depth 9, you can look it up in the transposition table, and if you've already searched
          it to depth 9, you might be able to avoid a long search.  A less obvious use of the main transposition table
          involves improving your move ordering.
        * You can use them to implement pawn structure hashing.  You can keep a key that is created only from the pawns
          that are on the board.  You can do sophisticated analysis of pawn structures, and store the result in a hash
          table for later retrieval.  In practice you end up with relatively few pawn structures that arise from a given
          start position, so the hit rate on this hash table is extremely high, so in essence you get to do all of the
          pawn structure evaluation you want for free.
        * You can make a smaller hash table, which you can use to detect repetitions in the current line, so you can
          detect perpetual check and other repetition draw cases.
        * You can use these to create an opening book that handles transpositions.
     */
    // from https://github.com/bhlangonijr/chesslib/blob/49599909c02fc652b15d89048ec88f8b707facf6/src/main/java/com/github/bhlangonijr/chesslib/Board.java
    private static final List<Long> keys = new ArrayList<>();
    private static final long RANDOM_SEED = 49109794719L;
    private static final int ZOBRIST_TABLE_SIZE = 2000;

    static {
        final XorShiftRandom random = new XorShiftRandom(RANDOM_SEED);
        for (int i = 0; i < ZOBRIST_TABLE_SIZE; i++) {
            long key = random.nextLong();
            keys.add(key);
        }
    }

    /**
     * Returns a Zobrist hash code value for this board. A Zobrist hashing assures the same position returns the same
     * hash value. It is calculated using the position of the pieces, the side to move, the castle rights and the en
     * passant target.
     *
     * @return a Zobrist hash value for this board
     * @see <a href="https://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing in Wikipedia</a>
     */
    public long getZobristKey() {
        return getZobristKey(this.getActivePlayerColor());
    }

    public long getZobristKey(PlayerColor color) {
        long hash = 0;
        for(char c : castlingRights.toCharArray()) {
            switch(c) {
                case 'K': hash ^= getCastleRightKey(1, PlayerColor.WHITE); break;
                case 'Q': hash ^= getCastleRightKey(2, PlayerColor.WHITE); break;
                case 'k': hash ^= getCastleRightKey(3, PlayerColor.BLACK); break;
                case 'q': hash ^= getCastleRightKey(4, PlayerColor.BLACK); break;
            }
        }

        for (int i = 0; i < 64; i++) {
            Piece piece = this.getBoard().get(i);
            if (piece != Piece.EMPTY)
                hash ^= getPieceSquareKey(piece, i);
        }

        hash ^= getSideKey(color);

        int epT = getEnPassantTargetAsInt();
        if (epT >= 0) {
            hash ^= getEnPassantKey(epT);
        }

        return hash;
    }

    private long getCastleRightKey(int castlingRightOrdinal, PlayerColor color) {
        return keys.get(3 * castlingRightOrdinal+ 300 + 3 * color.ordinal());
    }

    private long getSideKey(PlayerColor side) {
        return keys.get(3 * side.ordinal() + 500);
    }

    private long getEnPassantKey(int enPassantTarget) {
        return keys.get(3 * enPassantTarget + 400);
    }

    private long getPieceSquareKey(Piece piece, int square) {
        return keys.get(57 * piece.getIndex() + 13 * square);
    }

    public boolean isRepetition() {
        int n = 3;

        final int i = Math.min(getHistory().size() - 1, getFullmoveCounter() * 2 + getHalfmoveClock());
        if (getHistory().size() >= 4) {
            long lastKey = getHistory().get(getHistory().size() - 1);
            int rep = 0;
            for (int x = 4; x <= i; x += 2) {
                final long k = getHistory().get(getHistory().size() - x - 1);
                if (k == lastKey && ++rep >= n - 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
