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
    private static final int MOVE_LIST_CAPACITY = 220;

    record History(Move move, long[] boards, int enPassantTarget, boolean check, int castlingRights, Piece[] pieceCache) {};

    protected Player white;
    protected Player black;
    protected Board board;
    protected PlayerColor activePlayerColor;

    protected int enPassantTarget;
    private boolean check = false;
    private boolean limitMovesTo50 = true;
    private int depth = 0;
    private final LinkedList<Long> historyAsHashes = new LinkedList<>();
    private LinkedList<History> history = new LinkedList<>();

    // If neither side has the ability to castle, this field uses the character "-".
    // Otherwise, this field contains one or more letters: "K" if White can castle
    // kingside, "Q" if White can castle queenside, "k" if Black can castle
    // kingside, and "q" if Black can castle queenside. A situation that temporarily
    // prevents castling does not prevent the use of this notation.
    // protected String castlingRights;
    protected int castlingRights;

    public boolean canCastlingWhiteKingSide()  { return (castlingRights & CastlingHelper.CASTLING_WHITE_KING_SIDE) != 0; }
    public boolean canCastlingBlackKingSide() { return (castlingRights & CastlingHelper.CASTLING_BLACK_KING_SIDE) != 0; }
    public boolean canCastlingWhiteQueenSide() { return (castlingRights & CastlingHelper.CASTLING_WHITE_QUEEN_SIDE) != 0; }
    public boolean canCastlingBlackQueenSide() { return (castlingRights & CastlingHelper.CASTLING_BLACK_QUEEN_SIDE) != 0; }
    public boolean cannotCastle()              { return castlingRights == 0; }

    private void clearCastlingWhiteKingSide()  { castlingRights &= ~CastlingHelper.CASTLING_WHITE_KING_SIDE ; }
    private void clearCastlingBlackKingSide()  { castlingRights &= ~CastlingHelper.CASTLING_BLACK_KING_SIDE ; }
    private void clearCastlingWhiteQueenSide() { castlingRights &= ~CastlingHelper.CASTLING_WHITE_QUEEN_SIDE; }
    private void clearCastlingBlackQueenSide() { castlingRights &= ~CastlingHelper.CASTLING_BLACK_QUEEN_SIDE; }

    public String getCastlingRightsForFen() {
        if(castlingRights == 0) return "-";

        StringBuilder sb = new StringBuilder();

        if((castlingRights & CastlingHelper.CASTLING_WHITE_KING_SIDE ) != 0) sb.append(CastlingHelper.WHITE_KING_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_WHITE_QUEEN_SIDE) != 0) sb.append(CastlingHelper.WHITE_QUEEN_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_BLACK_KING_SIDE ) != 0) sb.append(CastlingHelper.BLACK_KING_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_BLACK_QUEEN_SIDE) != 0) sb.append(CastlingHelper.BLACK_QUEEN_CHAR);

        return sb.toString();
    }

    private void setInitialCastlingRights() {
        castlingRights =
            CastlingHelper.CASTLING_WHITE_KING_SIDE |
            CastlingHelper.CASTLING_WHITE_QUEEN_SIDE |
            CastlingHelper.CASTLING_BLACK_KING_SIDE |
            CastlingHelper.CASTLING_BLACK_QUEEN_SIDE;
    }

    private void clearCastlingRights() {
        castlingRights = 0;
    }

    private void setCastlingRightsFromFen(String fen) {
        if(fen.isBlank()) { clearCastlingRights(); }
        if(fen.indexOf(CastlingHelper.WHITE_KING_CHAR) >= 0) { castlingRights |= CastlingHelper.CASTLING_WHITE_KING_SIDE ; }
        if(fen.indexOf(CastlingHelper.WHITE_QUEEN_CHAR) >= 0) { castlingRights |= CastlingHelper.CASTLING_WHITE_QUEEN_SIDE; }
        if(fen.indexOf(CastlingHelper.BLACK_KING_CHAR) >= 0) { castlingRights |= CastlingHelper.CASTLING_BLACK_KING_SIDE ; }
        if(fen.indexOf(CastlingHelper.BLACK_QUEEN_CHAR) >= 0) { castlingRights |= CastlingHelper.CASTLING_BLACK_QUEEN_SIDE; }
    }

    protected int clock;
    private boolean outOfTime = false;

    // game with no players - used for analysis
    public Game() {
        this.depth = 0;
        this.board = new Board();
        this.board.setGame(this);
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setInitialCastlingRights();
        this.clearEnPassantTarget();
        this.setClock(0);
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
        this.setClock(game.getClock());

        setActivePlayerIsInCheck();
    }


    // game that can start midway through - used for analysis
    public Game(String fenString, int depth) {
        FenString fen = new FenString(fenString);
        this.depth = depth;
        this.board = new Board(fen.getPiecePlacement());
        this.board.setGame(this);

        this.setActivePlayerColor(fen.getActivePlayerColor());
        this.setCastlingRightsFromFen(fen.getCastlingRights());
        this.setEnPassantTargetFromFen(fen.getEnPassantTarget());
        this.setClock(fen.getFullmoveCounter() * 2 + fen.getHalfmoveClock()); // TODO: THis is not right.  We don't know how many moves have been taken.

        setActivePlayerIsInCheck();
    }

    public Game(Pgn pgn) {
        this.depth = 0;
        this.board = new Board();
        this.board.setGame(this);
        this.setActivePlayerColor(PlayerColor.WHITE);
        this.setInitialCastlingRights();
        this.clearEnPassantTarget();
        this.setClock(0);

        for(var move : pgn.getMoves()) {
//            LOGGER.info(move.toString());
            this.move(move.whiteMove());
            if(! move.blackMove().isBlank())
                this.move(move.blackMove());
//            this.getBoard().printOccupied();
        }

        setActivePlayerIsInCheck();  // this is a smell!  It should be handled in the postMoveAccounting() method, but it isn't.
    }

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

    public String getPiecePlacement() {
        return this.getBoard().toFen();
    }

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

    // TODO: Move to PawnMoves
    private void setEnPassantTargetFromFen(String target) {
        if(target.equals(PawnMoves.NO_EN_PASSANT))
            clearEnPassantTarget();
        else
            this.enPassantTarget = FenString.squareToLocation(target);
    }

    private void clearEnPassantTarget() {
        this.enPassantTarget = PawnMoves.NO_EN_PASSANT_VALUE;
    }

    private void setEnPassantTarget(int target) {
        this.enPassantTarget = target;
    }

    public String getEnPassantTargetAsFen() {
        if (this.enPassantTarget == -1) {
            return "-";
        }

        return FenString.locationToSquare(enPassantTarget);
    }

    public int getEnPassantTarget() {
        return this.enPassantTarget;
    }

    public boolean hasEnPassantTarget() { return this.enPassantTarget != PawnMoves.NO_EN_PASSANT_VALUE; }

    private void setCastlingRights(int castlingRights) {
        this.castlingRights = castlingRights;
    }

    public int getCastlingRights() {
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

    private void pawnMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        if(getActivePlayerColor() == PlayerColor.WHITE) {
            whitePawnMoves(playerMoves);
        } else {
            blackPawnMoves(playerMoves);
        }
    }

    private void blackPawnMoves(MoveList<Move> playerMoves) {
        long oneStep;
        oneStep = blackPawnOneStep(playerMoves);
        blackTwoStepsForward(playerMoves, oneStep);
        blackPawnAttacks(playerMoves);
    }

    private void whitePawnMoves(MoveList<Move> playerMoves) {
        long oneStep;
        oneStep = whitePawnOneStep(playerMoves);
        whiteTwoStepsForward(playerMoves, oneStep);
        whitePawnAttacks(playerMoves);
    }

    private void whiteTwoStepsForward(MoveList<Move> playerMoves, long oneStep) {
        long twoStep = ((oneStep & Board.RANK_3) << 8L) & ~getBoard().getOccupied();
        int diff = -16;

        for (int dest : Board.bitboardToArray(twoStep)) {
            Move m = new Move(Piece.WHITE_PAWN, dest + diff, dest, Move.NON_CAPTURE);
            long enPassantMask = ((1L << (dest + 1)) | (1L << (dest - 1))) & getBoard().getBlack();

            enPassantMask &= Board.RANK_4;

            if(enPassantMask != 0) {
                m.setEnPassant(dest - 8);
            }

            playerMoves.add(m);
        }
    }

    private void blackTwoStepsForward(MoveList<Move> playerMoves, long oneStep) {
        long twoStep = ((oneStep & Board.RANK_6) >> 8L) & ~getBoard().getOccupied();;
        int diff = 16;

        for (int dest : Board.bitboardToArray(twoStep)) {
            Move m = new Move(Piece.BLACK_PAWN, dest + diff, dest, Move.NON_CAPTURE);
            long enPassantMask = ((1L << (dest + 1)) | (1L << (dest - 1))) & getBoard().getWhite();

            enPassantMask &= Board.RANK_5;

            if(enPassantMask != 0) {
                m.setEnPassant(dest + 8);
            }

            playerMoves.add(m);
        }
    }

    private long whitePawnOneStep(MoveList<Move> playerMoves) {
        long oneStep = (board.getWhitePawns() << 8L) & ~board.getOccupied();
        int diff = -8;
        Piece piece = Piece.WHITE_PAWN;

        // promotions
        long promotions = oneStep & Board.RANK_8;
        oneStep &= ~Board.RANK_8;

        for (int dest : Board.bitboardToArray(oneStep)) {
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE));
        }

        for (int dest : Board.bitboardToArray(promotions)) {
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.WHITE_QUEEN));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.WHITE_BISHOP));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.WHITE_KNIGHT));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.WHITE_ROOK));
        }

        return oneStep;
    }

    private long blackPawnOneStep(MoveList<Move> playerMoves) {
        long oneStep = (board.getBlackPawns() >> 8L) & ~board.getOccupied();
        int diff = 8;
        Piece piece = Piece.BLACK_PAWN;

        // promotions
        long promotions = oneStep & Board.RANK_1;
        oneStep &= ~Board.RANK_1;

        for (int dest : Board.bitboardToArray(oneStep)) {
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE));
        }

        for (int dest : Board.bitboardToArray(promotions)) {
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.BLACK_QUEEN));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.BLACK_BISHOP));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.BLACK_KNIGHT));
            playerMoves.add(new Move(piece, dest + diff, dest, Move.NON_CAPTURE, Piece.BLACK_ROOK));
        }
        return oneStep;
    }

    private void blackPawnAttacks(MoveList<Move> playerMoves) {
        int attackCount;
        long attackBoard;
        long pawns = getBoard().getBlackPawns();
        long opponentBoard = getBoard().getWhite();
        boolean hasEnPassantTarget = hasEnPassantTarget();
        Piece piece = Piece.BLACK_PAWN;

        for(int i : Board.bitboardToArray(pawns)) {
            // capture
            attackBoard = PawnMoves.PAWN_ATTACKS[1][i];

            if(hasEnPassantTarget) {
                attackBoard &= (opponentBoard | (1L << getEnPassantTarget()));
            } else {
                attackBoard &= opponentBoard;
            }

            attackCount = PieceMoves.popCount(attackBoard);

            for(int j = 0; j < attackCount; j++) {
                int bitPos = Long.numberOfTrailingZeros(attackBoard);

                if((attackBoard & Board.RANK_1) != 0) {  // promoting
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.BLACK_QUEEN));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.BLACK_BISHOP));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.BLACK_KNIGHT));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.BLACK_ROOK));
                } else {
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE));
                }

                attackBoard &= (attackBoard - 1L);  // remove the bit
            }
        }
    }

    private void whitePawnAttacks(MoveList<Move> playerMoves) {
        int attackCount;
        long attackBoard;
        long pawns = getBoard().getWhitePawns();
        long opponentBoard = getBoard().getBlack();
        boolean hasEnPassantTarget = hasEnPassantTarget();
        Piece piece = Piece.WHITE_PAWN;
        for(int i : Board.bitboardToArray(pawns)) {
            // capture
            attackBoard = PawnMoves.PAWN_ATTACKS[0][i];

            if(hasEnPassantTarget) {
                attackBoard &= (opponentBoard | (1L << getEnPassantTarget()));
            } else {
                attackBoard &= opponentBoard;
            }

            attackCount = PieceMoves.popCount(attackBoard);

            for(int j = 0; j < attackCount; j++) {
                int bitPos = Long.numberOfTrailingZeros(attackBoard);

                if((attackBoard & Board.RANK_8) != 0) {  // promoting
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.WHITE_QUEEN));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.WHITE_BISHOP));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.WHITE_KNIGHT));
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE, Piece.WHITE_ROOK));
                } else {
                    playerMoves.add(new Move(piece, i, bitPos, Move.CAPTURE));
                }

                attackBoard &= (attackBoard - 1L);  // remove the bit
            }
        }
    }

    @Nullable
    private void cleanUpMoves(MoveList<Move> playerMoves) {
        // if in check, make sure any move takes the player out of check.
        MoveList<Move> toRemove = new MoveList<Move>(new ArrayList<Move>(playerMoves.size()));

        for (var move : playerMoves) {
            // can't use castling to get out of check
            if(getChecked() && move.isCastling()) {
                toRemove.add(move);
                continue;
            }

            move(move);

            // if the player remains in check after the move, then it is not valid.  Remove it from the moves list.
            // isPlayerInCheck(activePlayer) looks at the current players check state, whereas getChecked looks at the
            // checked state as if the turn was over so it looks at the opponents check state.
            if(isPlayerInCheck(getActivePlayerColor().otherColor()))  // move() changes the color of the player, so check to see if the previous move was valid
                toRemove.add(move);

            undo();
        }

        playerMoves.removeAll(toRemove);


    }

    //   8/K1b5/1k5p/7P/8/8/8/8 b - - 0 1
    //   K7/2b5/1k5p/7P/8/8/8/8 w - -
    private void kingMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBoard().getKingLocations(activePlayerColor);

        int kingLocation = locations[0];
        KingMoves rawMoves = new KingMoves(board, kingLocation);
        Piece king = board.get(kingLocation);
        Move m;

        // King: non capture
        for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
            m = new Move(king, kingLocation, dest, Move.NON_CAPTURE);
            addCastling(m);
            playerMoves.add(m);
        }

        // King: capture
        for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
            playerMoves.add(new Move(king, kingLocation, dest, Move.CAPTURE));
        }
    }

    private void addCastling(Move m) {
        int from = m.getFrom();
        int to = m.getTo();

        // white king side
        if(from == CastlingHelper.WHITE_INITIAL_KING_LOCATION &&
            to == CastlingHelper.WHITE_KING_SIDE_CASTLING_KING_LOCATION&&
            canCastlingWhiteKingSide()) {
            m.setCastling(
                CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION,
                CastlingHelper.WHITE_KING_SIDE_CASTLING_ROOK_LOCATION);
        } else if(from == CastlingHelper.WHITE_INITIAL_KING_LOCATION &&
            to == CastlingHelper.WHITE_QUEEN_SIDE_CASTLING_KING_LOCATION &&
            canCastlingWhiteQueenSide()) {
            m.setCastling(
                CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION,
                CastlingHelper.WHITE_QUEEN_SIDE_CASTLING_ROOK_LOCATION);
        } else if(from == CastlingHelper.BLACK_INITIAL_KING_LOCATION &&
            to == CastlingHelper.BLACK_KING_SIDE_CASTLING_KING_LOCATION &&
            canCastlingBlackKingSide()) {
            m.setCastling(
                CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION,
                CastlingHelper.BLACK_KING_SIDE_CASTLING_ROOK_LOCATION);
        } else if(from == CastlingHelper.BLACK_INITIAL_KING_LOCATION &&
            to == CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_KING_LOCATION &&
            canCastlingBlackQueenSide()) {
            m.setCastling(
                CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION,
                CastlingHelper.BLACK_QUEEN_SIDE_CASTLING_ROOK_LOCATION);
        }

        // white queen side
        // black king side
        // black queen site
    }

    private void queenMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBoard().getQueenLocations(activePlayerColor);

        for(int i : locations) {
            QueenMoves rawMoves = new QueenMoves(board, i);
            Piece piece = board.get(i);

            // Queen: non capture
            for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.NON_CAPTURE));
            }

            // Queen: capture
            for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.CAPTURE));
            }
        }
    }

    private void bishopMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBoard().getBishopLocations(activePlayerColor);

        for(int i : locations) {
            BishopMoves rawMoves = new BishopMoves(board, i);
            Piece piece = board.get(i);

            // Bishop: non capture
            for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.NON_CAPTURE));
            }

            // Bishop: capture
            for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.CAPTURE));
            }
        }
    }

    private void knightMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBoard().getKnightLocations(activePlayerColor);

        for(int i : locations) {
            KnightMoves rawMoves = new KnightMoves(board, i);
            Piece piece = board.get(i);

            // Knight: non capture
            for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.NON_CAPTURE));
            }

            // Knight: capture
            for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.CAPTURE));
            }
        }
    }

    private void rookMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBoard().getRookLocations(activePlayerColor);

        for(int i : locations) {
            RookMoves rawMoves = new RookMoves(board, i);
            Piece piece = board.get(i);

            // Rook: non capture
            for (int dest : Board.bitboardToArray(rawMoves.getNonCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.NON_CAPTURE));
            }

            // Rook: capture
            for (int dest : Board.bitboardToArray(rawMoves.getCaptureMoves())) {
                playerMoves.add(new Move(piece, i, dest, Move.CAPTURE));
            }
        }
    }

    private long rookCaptures(PlayerColor pc, int targetLocation) {
        long rooks = board.getRooks(pc);
        long rooksAttacks = getRookAttacksForSquare(targetLocation, 0L);
        return rooksAttacks & rooks;
    }

    private long getRookAttacksForSquare(int location, long currentPlayerBoard) {
        return StraightLineMoves.getRookAttacks(location, board.getOccupied()) & ~currentPlayerBoard;
    }

    private long bishopCaptures(PlayerColor pc, int targetLocation) {
        long bishops = board.getBishops(pc);
        long bishopAttacks = getBishopAttacksForSquare(targetLocation, 0L);
        return bishopAttacks & bishops;
    }

    private long queenCaptures(PlayerColor pc, int targetLocation) {
        long queens = board.getQueens(pc);
        long rooksAttacks = getRookAttacksForSquare(targetLocation, 0L);
        long bishopAttacks = getBishopAttacksForSquare(targetLocation, 0L);
        return (rooksAttacks | bishopAttacks) & queens;
    }

    private long getBishopAttacksForSquare(int location, long currentPlayerBoard) {
        return StraightLineMoves.getBishopAttacks(location, board.getOccupied()) & ~currentPlayerBoard;
    }

    private long pawnCaptures(PlayerColor color, int targetLocation) {
        return PawnMoves.PAWN_ATTACKS[color == PlayerColor.WHITE ? 0 : 1][targetLocation];
    }

    private long knightCaptures(int targetLocation) {
        return KnightMoves.KNIGHT_MOVES[targetLocation];
    }

    private long kingCaptures(int targetLocation) {
        return KingMoves.KING_MOVES[targetLocation];
    }

    private long getCurrentPlayerBoard() {
        return activePlayerColor == PlayerColor.WHITE ? board.getWhite() : board.getBlack();
    }

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

    private MoveList<Move> generateMovesFor(PlayerColor playerColor) {
        MoveList<Move> playerMoves = new MoveList<Move>(new ArrayList<Move>(MOVE_LIST_CAPACITY));;

        rookMoves(playerMoves, playerColor);
        knightMoves(playerMoves, playerColor);
        bishopMoves(playerMoves, playerColor);
        queenMoves(playerMoves, playerColor);
        kingMoves(playerMoves, playerColor);
        pawnMoves(playerMoves, playerColor);

        if (depth > 0)
            return playerMoves;

        cleanUpMoves(playerMoves);

        return playerMoves;
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
        return locationUnderAttack(playerColor.otherColor(), kingLocation);
    }

    private boolean locationUnderAttack(PlayerColor color, int location) {
        if(rookCaptures(color, location) != 0) return true;  // can the opposing player capture the king with their rooks?
        if(bishopCaptures(color, location) != 0) return true;
        if(queenCaptures(color, location) != 0) return true;

        if((pawnCaptures(color.otherColor(), location) & board.getPawns(color)) != 0) return true;
        if((knightCaptures(location) & board.getKnights(color)) != 0) return true;
        if((kingCaptures(location) & board.getKings(color)) != 0) return true;

        return false;
    }

    /*
     * Long Alegbraic notation without piece names is in the format
     * <FROM_AS_SQUARE_COORDS><TO_AS_SQUARE_COORDS><PROMOTION>
     * e.g.
     * - c2c3  <-- move pawn one space
     * - b7b8Q <-- queen promotion
     */
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
            updateMoveForCastling(m);
            m.updateForEnPassant(getBoard().getWhitePawns(), getBoard().getBlackPawns());
            removeEnPassantIfAttackingPieceIsPinned(m);
        }

        move(m);
    }

    void updateMoveForCastling(Move m) {
        int from = m.getFrom();
        int to = m.getTo();

        // if it is a castling
        if (from == 4) { // e1 -> 4
            if (to == 6 && canCastlingWhiteKingSide()) { // g1
                m.setCastling(7, 5);
            } else if (to == 2 && canCastlingWhiteQueenSide()) { // b1
                m.setCastling(0, 3);
            }
        } else if (from == 60) { // g8 || b8
            if (to == 62 && canCastlingBlackKingSide()) {
                m.setCastling(63, 61);
            } else if (to == 58 && canCastlingBlackQueenSide()) {
                m.setCastling(56, 59);
            }
        }
    }

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
        }

        move(move);
    }

    /*
     * expects en passant, castling, and promotion to be set in the move object.
     */
    public void move(Move move) {
        int removed = updateBoard(move);
        postMoveAccounting(move, removed);
    }

    private int updateBoard(Move move) {
        long[] boardsBeforeUpdate = getBoard().getCopyOfBoards();
        Piece[] copyOfPieceCache = Arrays.copyOf(getBoard().getPieceCache(), getBoard().getPieceCache().length);

        int removed = this.getBoard().update(move, getEnPassantTarget());

        removeEnPassantIfAttackingPieceIsPinned(move);
        recordMove(move, boardsBeforeUpdate, copyOfPieceCache);

        return removed;
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
            PieceMovesInterface pm = piece.generateMoves(this.board, tempLocation, getCastlingRights(), getEnPassantTarget());

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

        return new Move(piece, source, destination, capture);
    }

    public LinkedList<Long> getHistory() {
        return historyAsHashes;
    }

    private void postMoveAccounting(Move move, int removedLocation) {
        setEnPassantTarget(move.getEnPassantTarget());
        removeCastlingRightsFor(move, removedLocation);
        historyAsHashes.add(getZobristKey());
        switchActivePlayer();
        setActivePlayerIsInCheck();
        incrementClock();
    }

    private void recordMove(Move move, long[] boards, Piece[] pieceCache) {
        History h = new History(
            move,
            boards,
            getEnPassantTarget(),
            check,
            castlingRights,
            pieceCache
        );
        history.add(h);
    }

    public void undo() {
        History h = history.pollLast();
        getBoard().setBoards(h.boards);
        getBoard().setPieceCache(h.pieceCache);
        historyAsHashes.pollLast();  // drop the hash
        this.enPassantTarget = h.enPassantTarget();
        this.check = h.check();
        this.castlingRights = h.castlingRights();

        switchActivePlayer();
        decrementClock();
    }

    private void removeEnPassantIfAttackingPieceIsPinned(Move move) {
        // if the move doesn't trigger en passant, then exit
        if(move.getEnPassantTarget() == PawnMoves.NO_EN_PASSANT_VALUE)
            return;

        willEnPassantLeaveKingInCheck(move, move.getTo() - 1); // west
        willEnPassantLeaveKingInCheck(move, move.getTo() + 1); // east
    }

    private void willEnPassantLeaveKingInCheck(Move move, int location) {
        Move enPassantMove;
        Piece enPassantPiece;

        if (Board.rank(location) == Board.rank(move.getTo())) { // same rank
            enPassantPiece = getBoard().get(location);

            if(enPassantPiece.isPawn() &&
                enPassantPiece.getColor() != move.getPiece().getColor()) {
                enPassantMove = new Move(enPassantPiece, location, move.getEnPassantTarget(), true);
                // enPassantMove.setEnPassant(move.getEnPassantTarget());

                long[] boardsBackup = getBoard().getCopyOfBoards();
                Piece[] piecesBackup = getBoard().getCopyOfPieceCache();

                this.getBoard().update(enPassantMove, getEnPassantTarget());
                if(isPlayerInCheck(getActivePlayerColor().otherColor())) {
                    move.clearEnPassant();
                    setEnPassantTarget(move.getEnPassantTarget());  // clear the game state too.
                }
                this.getBoard().setBoards(boardsBackup);
                this.getBoard().setPieceCache(piecesBackup);
            }
        }
    }

    private void setPlayerInCheck(boolean inCheck) {
        this.check = inCheck;
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

    /*
     * If the King or their respective rooks move, then remove the castling option
     */
    private void removeCastlingRightsFor(Move m, int removedLocation) {
        if(m.isCapture()) {
            switch(removedLocation) {
                case CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingWhiteQueenSide();
                case CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingWhiteKingSide();
                case CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingBlackQueenSide();
                case CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingBlackKingSide();
            }
        }

        switch(m.getFrom()) {
            case CastlingHelper.WHITE_INITIAL_KING_LOCATION -> { clearCastlingWhiteQueenSide(); clearCastlingWhiteKingSide(); }
            case CastlingHelper.BLACK_INITIAL_KING_LOCATION -> { clearCastlingBlackQueenSide(); clearCastlingBlackKingSide(); }
            case CastlingHelper.WHITE_QUEEN_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingWhiteQueenSide();
            case CastlingHelper.WHITE_KING_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingWhiteKingSide();
            case CastlingHelper.BLACK_QUEEN_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingBlackQueenSide();
            case CastlingHelper.BLACK_KING_SIDE_INITIAL_ROOK_LOCATION -> clearCastlingBlackKingSide();
        }
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
        sb.append(this.getCastlingRightsForFen());
        sb.append(' ');
        sb.append(this.getEnPassantTargetAsFen());
        sb.append(' ');
        sb.append(this.getHalfMoveClock());
        sb.append(' ');
        sb.append(this.getFullMoveCounter());
        return sb.toString();
    }

    public String asFenNoCounters() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPiecePlacement());
        sb.append(' ');
        sb.append(this.getActivePlayerColor());
        sb.append(' ');
        sb.append(this.getCastlingRightsForFen());
        sb.append(' ');
        sb.append(this.getEnPassantTargetAsFen());
        return sb.toString();
    }
    
    public Board getBoard() {
        return this.board;
    }

    public boolean isOver() {
        return isCheckmated(getActivePlayerColor()) || hasResigned() || isStalemate() || hasInsufficientMaterials() || exceededMoves() || isRepetition();
    }

    public boolean exceededMoves() {
        return limitMovesTo50 && past50moves();
    }

    public boolean past50moves() {
        return clock > 50;
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
        return !getChecked() && generateMoves().isEmpty();
    }

    public boolean hasResigned() {
        return false;
    }

    public boolean getChecked() {
        return check;
    }

    // TODO: Can isChecked be removed and replaced with isPlayerInCheck?
    public boolean isChecked(PlayerColor playerColor) {
        return isChecked(generateMoves(), playerColor);
    }

    // TODO: Can isChecked be removed?
    public boolean isChecked(MoveList<Move> moves, PlayerColor playerColor) {
        int kingLocation = getBoard().getKingLocation(playerColor);
        if(kingLocation == -1) return true;

        for (Move move : moves) {
            // skip this move if it isn't trying to capture the king.
            if (!move.isCapture() || move.getTo() != kingLocation)
                continue;

            return true;
        }

        return false;
    }

    public boolean isDraw() {
        return getFullMoveCounter() * 50 >= 100 ||
            isStalemate() ||
            hasInsufficientMaterials() ||
            isRepetition();
    }

    public boolean isCheckmated(PlayerColor playerColor) {
        return getChecked() && generateMoves().isEmpty();
    }

    public void disable50MovesRule() {
        this.limitMovesTo50 = false;
    }

    public SortedMap<String, Integer> perftAtRoot(int depth) {
        SortedMap<String, Integer> perftResults = new TreeMap<>();

        MoveList<Move> moves = this.generateMoves();
        int moveCounter = 0;

        for (var move : moves) {
//            LOGGER.info("root move: {}", move);

            move(move);
            moveCounter = perft(depth - 1);
//            LOGGER.info("perft: {} {}", move, moveCounter);
            undo();
            perftResults.put(move.toLongSan(), moveCounter);
        }

        // printPerft(perftResults);

        return perftResults;
    }

    public int perft(int depth) {
        if (depth == 0)
            return 1;

//        LOGGER.info("perft FEN: {}", this.asFen());
        MoveList<Move> moves = this.generateMoves();

        int moveCounter = 0;

        for (var move : moves) {
//            LOGGER.info("move: {}", move);
            move(move);
            moveCounter += perft(depth - 1);
            undo();
        }

        return moveCounter;
    }

    public void printPerft(SortedMap<String, Integer> perftResults) {
        LOGGER.info("Perft Results");
        LOGGER.info("-------------");
        for(var r : perftResults.keySet()) {
            LOGGER.info("{} {}", r, perftResults.get(r));
        }

        LOGGER.info("Nodes searched: {}", perftResults.values().stream().reduce(0, Integer::sum));
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
    private static final List<Long> zorbistRandomKeys = new ArrayList<>();
    private static final long RANDOM_SEED = 49109794719L;
    private static final int ZOBRIST_TABLE_SIZE = 2000;

    static {
        final XorShiftRandom random = new XorShiftRandom(RANDOM_SEED);
        for (int i = 0; i < ZOBRIST_TABLE_SIZE; i++) {
            long key = random.nextLong();
            zorbistRandomKeys.add(key);
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
//        LOGGER.info("zobrist: {}, {}, {}, {}", asFen(), castlingRights, color, getEnPassantTargetAsInt());
        long hash = 0;

        if(canCastlingWhiteKingSide())  hash ^= getCastleRightKey(1, PlayerColor.WHITE);
        if(canCastlingWhiteQueenSide()) hash ^= getCastleRightKey(2, PlayerColor.WHITE);
        if(canCastlingBlackKingSide())  hash ^= getCastleRightKey(3, PlayerColor.BLACK);
        if(canCastlingBlackQueenSide()) hash ^= getCastleRightKey(4, PlayerColor.BLACK);

        // TODO: I think it will be faster to iterate over each board's bits so replace this algo.
        for (int i = 0; i < 64; i++) {
            Piece piece = this.getBoard().get(i);
            if (piece != Piece.EMPTY)
                hash ^= getPieceSquareKey(piece, i);
        }

        hash ^= getSideKey(color);

        int epT = getEnPassantTarget();
        if (epT >= 0) {
            hash ^= getEnPassantKey(epT);
        }

        return hash;
    }

    private long getCastleRightKey(int castlingRightOrdinal, PlayerColor color) {
        return zorbistRandomKeys.get(3 * castlingRightOrdinal + 300 + 3 * color.ordinal());
    }

    private long getSideKey(PlayerColor side) {
        return zorbistRandomKeys.get(3 * side.ordinal() + 500);
    }

    private long getEnPassantKey(int enPassantTarget) {
        return zorbistRandomKeys.get(3 * enPassantTarget + 400);
    }

    private long getPieceSquareKey(Piece piece, int square) {
        return zorbistRandomKeys.get(57 * piece.getIndex() + 13 * square);
    }

    public boolean isRepetition() {
        int n = 3;

        final int i = Math.min(getHistory().size() - 1, getFullMoveCounter() * 2 + getHalfMoveClock());
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
