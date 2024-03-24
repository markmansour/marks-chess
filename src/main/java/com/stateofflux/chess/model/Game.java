package com.stateofflux.chess.model;

import java.lang.invoke.MethodHandles;
import java.util.*;

import com.stateofflux.chess.model.pieces.*;
import com.stateofflux.chess.model.player.Player;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Only legal moves can happen through the Game object.  It acts as the
 * validation layer of the Board object.
 */
public class Game {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MOVE_LIST_CAPACITY = 220;

    record History(Move move, long[] boards, int enPassantTarget, boolean check, int castlingRights, Piece[] pieceCache, long hash) {}

    protected final Board board;
    protected PlayerColor activePlayerColor;

    private boolean check = false;
    private boolean limitMovesTo50 = true;
    private final int depth;
    private int clock;
    private boolean outOfTime = false;
    private int movesWithoutCaptureOrPawnMove = 0;

    private final List<History> historyOfMoves = new ArrayList<>();
    private final List<Move> toRemove = new ArrayList<>(64);

    // --------------------------- Constructors ---------------------------

    // game with no players - used for analysis
    public Game() {
        this.depth = 0;
        this.board = new Board();

        setActivePlayerColor(PlayerColor.WHITE);
        setClock(0);
    }

    public Game(String fenString) {
        this(fenString, 0);
    }

    // game that can start midway through - used for analysis
    public Game(String fenString, int depth) {
        FenString fen = new FenString(fenString);
        this.depth = depth;
        this.board = new Board(fen.getPiecePlacement());

        setActivePlayerColor(fen.getActivePlayerColor());
        board.setCastlingRightsFromFen(fen.getCastlingRights());
        board.setEnPassantTargetFromFen(fen.getEnPassantTarget());
        setClock(fen.getFullmoveCounter() * 2 + fen.getHalfmoveClock()); // TODO: THis is not right.  We don't know how many moves have been taken.
        setActivePlayerIsInCheck();
    }

    public Game(Pgn pgn) {
        this();

        for(var move : pgn.getMoves()) {
            this.move(move.whiteMove());
            if(! move.blackMove().isBlank())
                this.move(move.blackMove());
        }
    }

    // --------------------------- Static Methods ---------------------------
    static public Game fromSan(String san) {
        Game g = new Game();
        List<PgnMove> pgnMoves = Pgn.fromSan(san);

        for(var pgnMove : pgnMoves) {
            g.move(pgnMove.whiteMove());

            if(! pgnMove.blackMove().isBlank()) {
                g.move(pgnMove.blackMove());
            }

//            g.getBoard().printOccupied();
        }

        return g;
    }

    // --------------------------- Playing ---------------------------
    public void play(Player white, Player black) {
        play(white, black, 200);
    }

    public void play(Player white, Player black, int maxPly) {
        Player[] players = { white, black };
        int playerIndex = 0;
        Player currentPlayer;

        while(!isOver() && clock < maxPly) {
            currentPlayer = players[playerIndex % 2];
            Move move = currentPlayer.getNextMove(this);
            move(move);
            playerIndex++;

            logger.atInfo().log("{}/{}({}): move({}) -> {} ({}) - nv: {}",
                currentPlayer.getClass().getSimpleName(),
                currentPlayer.getEvaluator().toString(),
                currentPlayer.getSearchDepth(),
                move,
                asFen(),
                currentPlayer,
                currentPlayer.getNodesVisited());
        }
    }

    // --------------------------- Instance Methods ---------------------------
    public String getPiecePlacement() {
        return this.getBoard().toFen();
    }

    public Board getBoard() {
        return this.board;
    }

    private void removeEnPassantIfAttackingPieceIsPinned(Move move) {
        // if the move doesn't trigger en passant, then exit
        if(move.getEnPassantTarget() == PawnMoves.NO_EN_PASSANT_VALUE)
            return;

        boolean enPassantToTheEast = doesEnPassantPieceExist(move, move.getTo() - 1);
        boolean enPassantToTheWest = doesEnPassantPieceExist(move, move.getTo() + 1);
        boolean eastEnPassantExposesCheck = enPassantToTheEast && doesMoveExposeCheck(move, move.getTo() - 1);
        boolean westEnPassantExposesCheck = enPassantToTheWest && doesMoveExposeCheck(move, move.getTo() + 1);

        if(eastEnPassantExposesCheck && westEnPassantExposesCheck) { // two en passant and they both expose check
            move.clearEnPassant();
            board.clearEnPassantTarget();
        } else if(enPassantToTheEast && !enPassantToTheWest && eastEnPassantExposesCheck) {  // west only en passant
            move.clearEnPassant();
            board.clearEnPassantTarget();
        } else if(!enPassantToTheEast && enPassantToTheWest && westEnPassantExposesCheck) {  // east only en passant
            move.clearEnPassant();
            board.clearEnPassantTarget();
        }
    }

    private boolean doesMoveExposeCheck(Move move, int location) {
        boolean exposesCheck = false;

        Piece enPassantPiece = getBoard().get(location);
        Move enPassantMove = new Move(enPassantPiece, location, move.getEnPassantTarget(), true);

        // simplified move/undo.
        long[] boardsBackup = getBoard().copyOfBoards();
        Piece[] piecesBackup = getBoard().copyOfPieceCache();
        int backupEnPassant = getBoard().getEnPassantTarget();
        long hash = getZobristKey();

        this.getBoard().update(enPassantMove);
        board.setEnPassantTarget(move.getEnPassantTarget());

        if(isPlayerInCheck(getActivePlayerColor().otherColor())) {
            exposesCheck = true;
        }

        this.getBoard().setBoards(boardsBackup);
        this.getBoard().setPieceCache(piecesBackup);
        getBoard().setEnPassantTarget(backupEnPassant);

        board.calculateAllCacheBoards();  // this could also be backed up and restored rather than recalculated.
        board.forceZobristKey(hash);

        return exposesCheck;
    }

    private boolean doesEnPassantPieceExist(Move move, int location) {
        Piece enPassantPiece;

        if (Board.rank(location) == Board.rank(move.getTo())) { // same rank
            enPassantPiece = getBoard().get(location);

            return (enPassantPiece.isPawn() &&
                enPassantPiece.getColor() != move.getPiece().getColor());
        }

        return false;
    }

    // --------------------------- Players ---------------------------

    private void setActivePlayerColor(PlayerColor activePlayerColor) {
        if(this.activePlayerColor != null)
            board.updateZobristKeyFlipPlayer(this.activePlayerColor);  // xor out the old color

        this.activePlayerColor = activePlayerColor;
        board.updateZobristKeyFlipPlayer(this.activePlayerColor); // xor in the new color
    }

    public PlayerColor getActivePlayerColor() {
        return this.activePlayerColor;
    }

    public PlayerColor getWaitingPlayer() {
        return this.activePlayerColor.otherColor();
    }

    private void setActivePlayerIsInCheck() {
        setPlayerIsInCheck(getActivePlayerColor());
    }
    // review the other players moves that could attack the king

    private void setPlayerIsInCheck(PlayerColor playerColor) {
        this.check = isPlayerInCheck(playerColor);
    }

    private boolean isPlayerInCheck(PlayerColor playerColor) {
        int kingLocation = board.getKingLocation(playerColor);
        if(kingLocation == -1) return true;  // this only happens in testing scenarios.
        return board.locationUnderAttack(playerColor.otherColor(), kingLocation);
    }

    private void switchActivePlayer() {
        board.updateZobristKeyFlipPlayer(activePlayerColor);
        this.activePlayerColor = this.getWaitingPlayer();
        board.updateZobristKeyFlipPlayer(activePlayerColor);
    }
    // --------------------------- Moves ---------------------------

    public MoveList<Move> generateMoves() {
        return generateMovesFor(getActivePlayerColor());
    }
    // This method generates moves at a board level (batch) instead of calculating moves a single position at a time.

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
    public MoveList<Move> generateMovesFor(PlayerColor playerColor) {
        MoveList<Move> playerMoves = pseudoLegalMoves(playerColor);

        if (depth > 0)
            return playerMoves;

        cleanUpMoves(playerMoves);

        return playerMoves;
    }

    public MoveList<Move> pseudoLegalMoves() {
        return pseudoLegalMoves(getActivePlayerColor());
    }

    public MoveList<Move> pseudoLegalMoves(PlayerColor playerColor) {
        MoveList<Move> playerMoves = new MoveList<>(new ArrayList<>(MOVE_LIST_CAPACITY));

        // Note - pseudoLegalMoves is called during the Evaluation method and it takes about 30% of the overall computation time.
        // I've looked at optimizing the existing algos and I can't make big gains.  I need a different approach to make
        // big gains.  What I've tried:
        // * using static functions to generate captures and non captures instead of instantiating object
        // * returning arrays instead of passing in playerMoves - I did not try to eliminate MoveList entirely.
        //
        board.rookMoves(playerMoves, playerColor);
        board.knightMoves(playerMoves, playerColor);
        board.bishopMoves(playerMoves, playerColor);
        board.queenMoves(playerMoves, playerColor);
        board.kingMoves(playerMoves, playerColor);
        board.pawnMoves(playerMoves, playerColor);

        return playerMoves;
    }

    private void cleanUpMoves(MoveList<Move> playerMoves) {
        // if in check, make sure any move takes the player out of check.
        toRemove.clear();

        for (var move : playerMoves) {
            // can't use castling to get out of check
            if(isChecked() && move.isCastling()) {
                toRemove.add(move);
                continue;
            }

            move(move);

            // is the other play in check?
            if(isPlayerInCheck(getActivePlayerColor().otherColor()))  // move() changes the color of the player, so check to see if the previous move was valid
                toRemove.add(move);

            undo();
        }

        playerMoves.removeAll(toRemove);
    }

    /*
     * Long Algebraic notation without piece names is in the format
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

        Move m = new Move(piece, fromSquare, toSquare, board.get(toSquare) != Piece.EMPTY);

        if((piece == Piece.WHITE_PAWN && toSquare >= 56) || (piece == Piece.BLACK_PAWN && toSquare <= 7))
            m.setPromotion(promotion);
        else {
            updateMoveForCastling(m);
            m.updateForEnPassant(getBoard().getWhitePawnBoard(), getBoard().getBlackPawnBoard());
            removeEnPassantIfAttackingPieceIsPinned(m);
        }

        move(m);
    }

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
        logger.atDebug().log("action ({}): {}", getActivePlayerColor(), action);
        Move move = sanToMove(action);

        move(move);
    }

    public Move sanToMove(String action) {
        Piece promotionPiece;
        Move move;

        // Castling
        move = extractCastlingMove(action);

        if (move == null) {
            // Promotion
            int promotionMarker = action.indexOf('=');
            if (promotionMarker >= 0) {
                promotionPiece = Piece.getPieceByPieceChar(action.substring(promotionMarker + 1, promotionMarker + 2));
                action = action.substring(0, promotionMarker);
            } else
                promotionPiece = Piece.EMPTY;

            move = extractMoveFromAlgebraicNotation(action);
            move.setPromotion(promotionPiece);  // ok to set to null
            move.updateForEnPassant(getBoard().getWhitePawnBoard(), getBoard().getBlackPawnBoard());
        }

        return move;
    }

    public void move(Move move) {
        long[] boardsBeforeUpdate = getBoard().copyOfBoards();
        Piece[] copyOfPieceCache = Arrays.copyOf(getBoard().getPieceCache(), getBoard().getPieceCache().length);

        // TODO: the depth of historyOfMoves is only as deep as the depth traversal (so no very large - currently depth 6 max)
        //       Therefore convert this to a fixed size array (and reuse the arrays instead of reallocated them).
        historyOfMoves.add(new History(
            move,
            boardsBeforeUpdate,
            board.getEnPassantTarget(),
            check,
            board.getCastlingRights(),
            copyOfPieceCache,
            board.getZobristKey()
        ));

        updateBoard(move);
        switchActivePlayer();
        setActivePlayerIsInCheck();
        incrementClock();
        update50MoveRule(move);
    }

    public Move undo() {
        int size = historyOfMoves.size();
        History h = historyOfMoves.remove(size - 1);

        // restore the board
        getBoard().setBoards(h.boards);          // updates the piece oriented views
        getBoard().setPieceCache(h.pieceCache);  // updates the board oriented views
        board.setEnPassantTarget(h.enPassantTarget());

        check = h.check();

        board.setCastlingRights(h.castlingRights());

        switchActivePlayer();

        board.forceZobristKey(h.hash);

        decrementClock();

        return h.move;
    }

    private void update50MoveRule(Move move) {
        if(move.isCapture() || move.getPiece().equals(Piece.WHITE_PAWN) || move.getPiece().equals(Piece.BLACK_PAWN))
            movesWithoutCaptureOrPawnMove = 0;
        else
            movesWithoutCaptureOrPawnMove++;
    }

    void updateMoveForCastling(Move m) {
        int from = m.getFrom();
        int to = m.getTo();

        // if it is a castling
        if (from == 4) { // e1 -> 4
            if (to == 6 && board.canCastlingWhiteKingSide()) { // g1
                m.setCastling(7, 5);
            } else if (to == 2 && board.canCastlingWhiteQueenSide()) { // b1
                m.setCastling(0, 3);
            }
        } else if (from == 60) { // g8 || b8
            if (to == 62 && board.canCastlingBlackKingSide()) {
                m.setCastling(63, 61);
            } else if (to == 58 && board.canCastlingBlackQueenSide()) {
                m.setCastling(56, 59);
            }
        }
    }

    private void updateBoard(Move move) {
        int removed = this.getBoard().update(move);
        removeCastlingRightsFor(move, removed);
        board.setEnPassantTarget(move.getEnPassantTarget());   // set en passant
        removeEnPassantIfAttackingPieceIsPinned(move);         // determine if it should be removed (or not)
        board.setEnPassantTarget(move.getEnPassantTarget());   // set the en passant with the updated info
    }

    public String getMoveHistory() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < historyOfMoves.size(); i++) {
            if(i % 2 == 0) sb.append((i / 2) + 1).append(". ");
            sb.append(historyOfMoves.get(i).move.toLongSan()).append(" ");
        }

        return sb.toString();
    }

    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "sourceHintWithoutPiece may not be present")
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
            case Piece.KING_ALGEBRAIC,
                Piece.QUEEN_ALGEBRAIC,
                Piece.ROOK_ALGEBRAIC,
                Piece.BISHOP_ALGEBRAIC,
                Piece.KNIGHT_ALGEBRAIC
                -> sourceHintWithoutPiece = sourceHint.substring(1);
            default -> sourceHintWithoutPiece = sourceHint;
        }

        char rankSpecified = 0;  // 1-8
        char fileSpecified = 0;  // a-h
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
                case Piece.KING_ALGEBRAIC -> this.getBoard().getKingLocations(this.getActivePlayerColor());
                case Piece.QUEEN_ALGEBRAIC -> this.getBoard().getQueenLocations(this.getActivePlayerColor());
                case Piece.BISHOP_ALGEBRAIC -> this.getBoard().getBishopLocations(this.getActivePlayerColor());
                case Piece.KNIGHT_ALGEBRAIC -> this.getBoard().getKnightLocations(this.getActivePlayerColor());
                case Piece.ROOK_ALGEBRAIC -> this.getBoard().getRookLocations(this.getActivePlayerColor());
                default -> this.getBoard().getPawnLocations(this.getActivePlayerColor());
            };

        Piece piece = null;

        for (int possibleSourceLocation : possibleSourceLocations) {
            tempLocation = possibleSourceLocation;
            piece = this.getBoard().get(tempLocation);
            PieceMovesInterface pm = piece.generateMoves(this.board, tempLocation, board.getCastlingRights(), board.getEnPassantTarget());

            // if the piece being reviewed isn't in the rank specified then skip over it
            if (rankSpecified != 0 && (('1' + (Board.rank(tempLocation))) != rankSpecified)) {
                continue;
            }

            // if the piece being reviewed isn't in the file specified then skip over it
            if (fileSpecified != 0 && (('a' + Board.file(tempLocation)) != fileSpecified)) {
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

        Move move = new Move(piece, source, destination, capture);
        if(capture) move.setCapturePiece(board.get(destination));

        return move;
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
                sourceLocation = CastlingHelper.BLACK_INITIAL_KING_LOCATION;
                destinationLocation = CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_KING_LOCATION;
                secondarySourceLocation = CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION;
                secondaryDestinationLocation = CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION;
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                castlingPiece = Piece.WHITE_KING;
                sourceLocation = CastlingHelper.WHITE_INITIAL_KING_LOCATION;
                destinationLocation = CastlingHelper.WHITE_QUEEN_SIDE_CASTLING_KING_LOCATION;
                secondarySourceLocation = CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION;
                secondaryDestinationLocation = CastlingHelper.WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION;
            }
        } else if (action.equals("O-O")) { // king side castle
            if (this.getActivePlayerColor() == PlayerColor.BLACK) {
                castlingPiece = Piece.BLACK_KING;
                sourceLocation = CastlingHelper.BLACK_INITIAL_KING_LOCATION;
                destinationLocation = CastlingHelper.BLACK_KING_SIDE_CASTLING_KING_LOCATION;
                secondarySourceLocation = CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION;
                secondaryDestinationLocation = CastlingHelper.BLACK_KING_SIDE_CASTLING_ROOK_LOCATION;
            } else if (this.getActivePlayerColor() == PlayerColor.WHITE) {
                castlingPiece = Piece.WHITE_KING;
                sourceLocation = CastlingHelper.WHITE_INITIAL_KING_LOCATION;
                destinationLocation = CastlingHelper.WHITE_KING_SIDE_CASTLING_KING_LOCATION;
                secondarySourceLocation = CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION;
                secondaryDestinationLocation = CastlingHelper.WHITE_KING_SIDE_CASTLING_ROOK_LOCATION;
            }
        }

        if(castlingPiece != null) {
            move = new Move(castlingPiece, sourceLocation, destinationLocation, Move.NON_CAPTURE);
            move.setCastling(secondarySourceLocation, secondaryDestinationLocation);

            return move;
        }

        return null;
    }

    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "the default case is a no-op")
    private void removeCastlingRightsFor(Move m, int removedLocation) {
        if(m.isCapture()) {
            switch(removedLocation) {
                case CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingWhiteQueenSide(); break;
                case CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION:  board.clearCastlingWhiteKingSide(); break;
                case CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingBlackQueenSide(); break;
                case CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION:  board.clearCastlingBlackKingSide(); break;
            }
        }

        switch(m.getFrom()) {
            case CastlingHelper.WHITE_INITIAL_KING_LOCATION: board.clearCastlingWhite(); break;
            case CastlingHelper.BLACK_INITIAL_KING_LOCATION: board.clearCastlingBlack(); break;
            case CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingWhiteQueenSide(); break;
            case CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingWhiteKingSide(); break;
            case CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingBlackQueenSide(); break;
            case CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION: board.clearCastlingBlackKingSide(); break;
        }
    }

    /*
     * <FEN> ::= <Piece Placement>
     * ' ' <Side to move>
     * ' ' <Castling ability>
     * ' ' <En passant target square>
     * ' ' <HalfMove clock>
     * ' ' <FullMove counter>
     */
    public String asFen() {
        return this.getPiecePlacement() +
            ' ' +
            this.getActivePlayerColor() +
            ' ' +
            board.getCastlingRightsForFen() +
            ' ' +
            this.board.getEnPassantTargetAsFen() +
            ' ' +
            this.getHalfMoveClock() +
            ' ' +
            this.getFullMoveCounter();
    }

    public String asFenNoCounters() {
        return this.getPiecePlacement() +
            ' ' +
            this.getActivePlayerColor() +
            ' ' +
            board.getCastlingRightsForFen() +
            ' ' +
            this.board.getEnPassantTargetAsFen();
    }

    public Move getLastMove() {
        if(historyOfMoves.isEmpty())
            return null;

        return historyOfMoves.get(historyOfMoves.size() - 1).move();
    }

    // --------------------------- check game state ---------------------------

    public boolean isChecked() {
        return check;
    }

    public boolean isDraw() {
        return exceededMoves() ||
            isStalemate() ||
            hasInsufficientMaterials() ||
            isRepetition();
    }

    public boolean isCheckmated() {
        return isChecked() && generateMoves().isEmpty();
    }

    // 3-fold repetition
    // https://en.wikipedia.org/wiki/Threefold_repetition
    public boolean isRepetition() {
        int n = 2;
        long[] temp = historyOfMoves.stream().mapToLong(x -> x.hash).toArray();

        int length = temp.length;
        if (length > 3) {
            long lastKey = getZobristKey();
            int rep = 0;

            // looking for two other matches in the history, moving in reverse order, and only looking at moves for the same color (-2).
            for(int x = length - 4; x >= 0; x -= 2) {
                if(temp[x] == lastKey && ++rep >= n)
                    return true;
            }
        }
        return false;
    }

    public boolean isOver() {
        return isCheckmated() || hasResigned() || isStalemate() || hasInsufficientMaterials() || exceededMoves() || isRepetition() || isSpinning();
    }

    private boolean isSpinning() {
        return this.clock > 4000;
    }

    public boolean exceededMoves() {
        return limitMovesTo50 && past50moves();
    }

    public boolean past50moves() {
        return movesWithoutCaptureOrPawnMove >= 50;
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
        return !isChecked() && generateMoves().isEmpty();
    }

    public boolean hasResigned() {
        return false;
    }

    public long getZobristKey() {
        return board.getZobristKey();
    }
    public long calculateFullZorbistKey() {
        return board.calculateFullZorbistKey(getActivePlayerColor());
    }

    // --------------------------- clocks ---------------------------
    public void setClock(int clock) { this.clock = clock; }
    public int getClock() { return this.clock; }
    public void incrementClock() { this.clock++; }
    public void decrementClock() { this.clock--; }

    public int getFullMoveCounter() {
        return this.clock >> 1;  // divide by 2
    }

    public int getHalfMoveClock() {
        return this.clock & 1;  // module 2
    }

    // --------------------------- Performance and Debugging ---------------------------
    public SortedMap<String, Long> perftAtRoot(int depth) {
        SortedMap<String, Long> perftResults = new TreeMap<>();

        MoveList<Move> moves = this.generateMoves();

        long moveCounter;

        for (var move : moves) {
//            logger.atInfo().log("root move: {}", move);

            if(depth > 1) {
                move(move);
                moveCounter = perft(depth - 1);
//            logger.atInfo().log("perft: {} {}", move, moveCounter);
                undo();
            } else {
                moveCounter = 1;
            }

            perftResults.put(move.toLongSan(), moveCounter);
        }

        // printPerft(perftResults);

        return perftResults;
    }

    public long perft(int depth) {
//        logger.atInfo().log("perft FEN: {}", this.asFen());
        MoveList<Move> moves = this.generateMoves();

        if (depth == 1)
            return moves.size();

        long moveCounter = 0;

        for (var move : moves) {
//            logger.atInfo().log("move: {}", move);
            move(move);
            moveCounter += perft(depth - 1);
            undo();
        }

        return moveCounter;
    }

    public void printPerft(SortedMap<String, Long> perftResults) {
        logger.atInfo().log("Perft Results");
        logger.atInfo().log("-------------");
        for(var r : perftResults.entrySet()) {
            logger.atInfo().log("{} {}", r.getKey(), r.getValue());
        }

        logger.atInfo().log("Nodes searched: {}", perftResults.values().stream().reduce(0L, Long::sum));
    }

    public void printOccupied() {
        board.printOccupiedBoard();

        logger.atDebug().log("   abcdefgh");
        logger.atDebug().log("--------------------------");
        logger.atDebug().log("FEN:           {}", asFen());
        logger.atDebug().log("isOver:        {}", isOver());
        logger.atDebug().log("Winner is:     {}", getWinner());
        logger.atDebug().log("move count:    {}", historyOfMoves.size());
        logger.atDebug().log("moves:         {}", getMoveHistory());
        logger.atDebug().log("");
        logger.atDebug().log("isCheckmated:  {}", isCheckmated());
        logger.atDebug().log("isDraw?:       {}", isDraw());
        logger.atDebug().log("hasResigned:   {}", hasResigned());
        logger.atDebug().log("isStalemate:   {}", isStalemate());
        logger.atDebug().log("hasRepeated:   {}", isRepetition());
        logger.atDebug().log("");
        logger.atDebug().log("exceededMoves: {}", exceededMoves());
        logger.atDebug().log("is spinning:   {}", isSpinning());
        logger.atDebug().log("hasInsufficientMaterials: {}", hasInsufficientMaterials());
        logger.atDebug().log("--------------------------");
    }

    public String getWinner() {
        if(isDraw()) return "Draw";
        if(isStalemate()) return "Stalemate";
        if(isCheckmated()) return "Checkmate: " + getActivePlayerColor().otherColor();
        if(exceededMoves()) return "Moves exceeded";
        return "unknown";
    }
}
