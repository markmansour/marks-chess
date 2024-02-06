package com.stateofflux.chess.model;

import java.util.Arrays;

import com.stateofflux.chess.model.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Board {
    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    public static final long RANK_1 = 0xffL;
    public static final long RANK_2 = 0xff00L;
    public static final long RANK_3 = 0xff0000L;
    public static final long RANK_4 = 0xff000000L;
    public static final long RANK_5 = 0xff00000000L;
    public static final long RANK_6 = 0xff0000000000L;
    public static final long RANK_7 = 0xff000000000000L;
    public static final long RANK_8 = 0xff00000000000000L;

    public static final long FILE_H = 0x8080808080808080L;
    public static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_F = 0x2020202020202020L;
    public static final long FILE_E = 0x1010101010101010L;
    public static final long FILE_D = 0x808080808080808L;
    public static final long FILE_C = 0x404040404040404L;
    public static final long FILE_B = 0x202020202020202L;
    public static final long FILE_A = 0x101010101010101L;
    private ZobristHasher zobristHasher;

    private long[] boards = new long[Piece.SIZE];

    // caches
    private long occupiedBoard;
    private long blackBoard;
    private long whiteBoard;
    private long blackBoardWithoutKing;
    private long whiteBoardWithoutKing;
    private Piece[] pieceCache = new Piece[64];
    private int enPassantTarget;

    // instance vars
    protected int castlingRights;

    // --------------------------- Constructors ---------------------------
    /**
     * RANKS:
     *   8 | 56 57 58 59 60 61 62 63 (MSB,
     *   7 | 48 49 50 51 52 53 54 55 left)
     *   6 | 40 41 42 43 44 45 46 47
     *   5 | 32 33 34 35 36 37 38 39
     *   4 | 24 25 26 27 28 29 30 31
     *   3 | 16 17 18 19 20 21 22 23
     *   2 |  8  9 10 11 12 13 14 15
     *   1 |  0  1  2  3  4  5  6  7 (LSB, right)
     * -------------------------------------------
     * FILES: a  b  c  d  e  f  g  h
     *
     * 8 ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
     * 7 ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
     * 6
     * 5
     * 4
     * 3
     * 2 ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
     * 1 ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
     * a b c d e f g h
     *
     */
    public Board() {
        boards = new long[Piece.SIZE];

        // Consider moving these to an ENUM
        this.boards[Piece.WHITE_KING.getIndex()] = 1L << 4;
        this.boards[Piece.WHITE_QUEEN.getIndex()] = 1L << 3;
        this.boards[Piece.WHITE_ROOK.getIndex()] = 1L | 1L << 7;
        this.boards[Piece.WHITE_BISHOP.getIndex()] = 1L << 2 | 1L << 5;
        this.boards[Piece.WHITE_KNIGHT.getIndex()] = 1L << 1 | 1L << 6;
        this.boards[Piece.WHITE_PAWN.getIndex()] = 255L << 8;

        this.boards[Piece.BLACK_KING.getIndex()] = 1L << 60;
        this.boards[Piece.BLACK_QUEEN.getIndex()] = 1L << 59;
        this.boards[Piece.BLACK_ROOK.getIndex()] = 1L << 63 | 1L << 56;
        this.boards[Piece.BLACK_BISHOP.getIndex()] = 1L << 58 | 1L << 61;
        this.boards[Piece.BLACK_KNIGHT.getIndex()] = 1L << 57 | 1L << 62;
        this.boards[Piece.BLACK_PAWN.getIndex()] = 255L << 48;

        populatePieceCache();
        calculateAllCacheBoards();

        initializeZobristKey(PlayerColor.WHITE);  // needs the caches populated for it to work.
    }

    /*
     * Build a board using a fen string
     */
    public Board(String fen, PlayerColor playerColor) {
        initializeZobristKey(playerColor);
        this.populate(fen);
        populatePieceCache();
        calculateAllCacheBoards();
    }

    // --------------------------- Static Methods ---------------------------
    public static int rank(int location) {
        return location >> 3;   // div 8
    }

    public static int file(int location) {
        return location & 0x7;  // modulo 8
    }

    public static int countSetBits(long n) {
        // base case
        if (n == 0)
            return 0;
        else
            return 1 + countSetBits(n & (n - 1L));
    }

    /** returns locations from 0-63 **/
    public static int[] bitboardToArray(long l) {
        int bitsSet = countSetBits(l);
        int[] result = new int[bitsSet];
        int counter = 0;

        for (int i = 0; i < 64 && counter < bitsSet; i++) {
            if ((l & (1L << i)) != 0) {
                result[counter++] = i;
            }
        }

        return result;
    }

    // --------------------------- Static Debugging Methods ---------------------------
    public static void printOccupied(long board) {
        String reversedLong = longToReversedBinaryString(board);

        for (int rank = 7; rank >= 0; rank--) {
            LOGGER.info("{}: {}",
                rank + 1,
                reversedLong.substring(rank * 8, (rank + 1) * 8).replace('0', '.'));
        }

        LOGGER.info("   abcdefgh");
    }

    public static String longToReversedBinaryString(long l) {
        StringBuilder sb = new StringBuilder();
        sb.append("0".repeat(Long.numberOfLeadingZeros(l)));
        sb.append(Long.toBinaryString(l));

        return sb.reverse().toString();
    }

    // --------------------------- Instance Methods ---------------------------

    void printOccupiedBoard() {
        StringBuilder prettyBoard = new StringBuilder(64);

        for (int i = 0; i < 64; i++) {
            prettyBoard.insert(i, get(i));
        }

        CharSequence[] ranks = new CharSequence[8];

        for (int i = 7; i >= 0; i--) {
            ranks[i] = prettyBoard.subSequence(i * 8, (i + 1) * 8);
            LOGGER.info("{}: {}", i + 1, ranks[i]);
        }
    }

    public void setBoards(long[] boards) {
        this.boards = Arrays.copyOf(boards, boards.length);
        calculateAllCacheBoards();
    }

    public long[] getBoards() {
        return boards;
    }

    private void populate(String fen) {
        char[] fenCh = fen.toCharArray();
        int rank = 7;
        int location = 56;

        for (char element : fenCh) {
            if (element == '/') {
                if (Board.file(location) != 0)
                    throw new IllegalArgumentException("Invalid FEN: " + fen);

                location = --rank * 8;

                continue;
            }

            // break if the fenCH[i] is a digit
            if (Character.isDigit(element)) {
                location += Character.digit(element, 10);
                continue;
            }

            location = set(element, location);
        }
    }

    public void setByBoard(Piece piece, int boardIndex, int location) {
        assert location >= 0;
        this.boards[boardIndex] |= (1L << location);
        zobristHasher.updateZobristKeyWhenSetting(piece, location);
        pieceCache[location] = Piece.getPieceByIndex(boardIndex);
    }

    public void clearByBoard(Piece piece, int boardIndex, int location) {
        this.boards[boardIndex] &= ~(1L << location);
        zobristHasher.updateZobristKeyWhenSetting(piece, location);
        pieceCache[location] = Piece.EMPTY;
    }

    protected void clearLocation(int location) {
        long bitToClear = 1L << location;

        for(int i = 0; i < boards.length; i++) {
            if((boards[i] & bitToClear) != 0) {
                // clear the bit at the location on all boards
                zobristHasher.clearZorbistMask(bitToClear);
                boards[i] &= ~bitToClear;
            }
        }

        pieceCache[location] = Piece.EMPTY;
    }

    public int set(char element, int location) {
        for (Piece piece : Piece.values()) {
            if (element == piece.getPieceChar()) {
                if (piece == Piece.EMPTY)
                    throw new IllegalArgumentException("Cannot place empty piece");

                setByBoard(piece, piece.getIndex(), location);
                location++;
                break;
            }
        }
        return location;
    }

    /*
     * return the character representing a piece
     */

    public Piece get(int location) {
        return pieceCache[location];
    }

    protected int getBoardIndex(int location) {
        for (int index = 0; index < this.boards.length; index++) {
            if ((this.boards[index] & (1L << location)) != 0)
                return index;
        }

        throw new AssertionError("Location not found: " + location);
    }

    public long getBlackKingBoard()   { return boards[Piece.BLACK_KING.getIndex()]; }
    public long getBlackQueenBoard()  { return boards[Piece.BLACK_QUEEN.getIndex()]; }
    public long getBlackBishopBoard() { return boards[Piece.BLACK_BISHOP.getIndex()]; }
    public long getBlackKnightBoard() { return boards[Piece.BLACK_KNIGHT.getIndex()]; }
    public long getBlackRookBoard()   { return boards[Piece.BLACK_ROOK.getIndex()]; }
    public long getBlackPawnBoard()   { return boards[Piece.BLACK_PAWN.getIndex()]; }

    public long getWhiteKingBoard()   { return boards[Piece.WHITE_KING.getIndex()]; }
    public long getWhiteQueenBoard()  { return boards[Piece.WHITE_QUEEN.getIndex()]; }
    public long getWhiteBishopBoard() { return boards[Piece.WHITE_BISHOP.getIndex()]; }
    public long getWhiteKnightBoard() { return boards[Piece.WHITE_KNIGHT.getIndex()]; }
    public long getWhiteRookBoard()   { return boards[Piece.WHITE_ROOK.getIndex()]; }
    public long getWhitePawnBoard()   { return boards[Piece.WHITE_PAWN.getIndex()]; }

    /*
     * Move a piece on the board, but do not perform validation.
     * return the removed location
     */
    public int update(Move m, int gameEnPassantState) {
        boolean standardMove = true;
        int removed;
        Piece removedPiece;

        int fromBoardIndex = this.getBoardIndex(m.getFrom());
        clearByBoard(m.getPiece(), fromBoardIndex, m.getFrom()); // clear
        removed = m.getTo();
        removedPiece = get(m.getTo());
        this.clearLocation(m.getTo());  // take the destination piece off the board if it exists.  It may be on any bitboard

        // if promotion
        if(m.isPromoting()) {
            fromBoardIndex = m.getPromotionPiece().getIndex();
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set promoted piece
            standardMove = false;
        }

        if(m.isEnPassant()) {
            int target = m.getEnPassantTarget();
            removed = target;
            removedPiece = get(target);
            this.clearLocation(target);  // clear en passant target
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set new pawn location
            standardMove = false;
        }

        // remove the piece that created the en passant scenario.
        if(m.isEnPassantCapture(gameEnPassantState)) {
            int target = m.getTo();

            int sourceFile, destFile;
            sourceFile = Board.file(m.getFrom());
            destFile = Board.file(target);

            // assert(sourceFile != destFile);
            if(sourceFile < destFile)
                removed = m.getFrom() + 1;
            else if(sourceFile > destFile)
                removed = m.getFrom() - 1;

            removedPiece = get(removed);
            this.clearLocation(removed);  // clear en passant target

            // boardIndex doesn't change (pawn to pawn)
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set new pawn location
            standardMove = false;
        }

        // if castling - king already moved
        if(m.isCastling()) {
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set king location

            int toBoardIndex = this.getBoardIndex(m.getSecondaryFrom());
            clearByBoard(m.getPiece(), toBoardIndex, m.getSecondaryFrom()); // clear
            setByBoard(m.getPiece(), toBoardIndex, m.getSecondaryTo()); // set rook location

            standardMove = false;
        }

        if(standardMove) {
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set
        }

        calculateAllCacheBoards();

        if(removedPiece != Piece.EMPTY)
            m.setCapturePiece(removedPiece);

        return removed;
    }

    public long getOccupied() {
        return this.occupiedBoard;
    }

    public void calculateOccupied() {
        long usedBoard = 0L;

        for (long board : boards) {
            usedBoard |= board;
        }

        this.occupiedBoard = usedBoard;
    }

    public boolean hasBlackKingOnly() {
        return getBlackWithoutKing() == 0;
    }

    private boolean blackHasOnlyOneMinorPiece() {
        int knights = getKnightLocations(PlayerColor.BLACK).length;
        int bishops = getBishopLocations(PlayerColor.BLACK).length;

        return
            (this.boards[Piece.BLACK_PAWN.getIndex()] == 0) &&
                (this.boards[Piece.BLACK_ROOK.getIndex()] == 0) &&
                (this.boards[Piece.BLACK_QUEEN.getIndex()] == 0) &&
                (knights + bishops == 1);
    }

    public boolean blackHasOnlyTwoKnights() {
        int knights = getKnightLocations(PlayerColor.BLACK).length;

        return
            (this.boards[Piece.BLACK_PAWN.getIndex()] == 0) &&
            (this.boards[Piece.BLACK_BISHOP.getIndex()] == 0) &&
                (this.boards[Piece.BLACK_ROOK.getIndex()] == 0) &&
                (this.boards[Piece.BLACK_QUEEN.getIndex()] == 0) &&
                (knights == 2);
    }

    public boolean blackHasAllOriginalPieces() {
        return getPawnLocations(PlayerColor.BLACK).length == 8 &&
            getRookLocations(PlayerColor.BLACK).length == 2 &&
            getKnightLocations(PlayerColor.BLACK).length == 2 &&
            getBishopLocations(PlayerColor.BLACK).length == 2 &&
            getQueenLocations(PlayerColor.BLACK).length == 1;
    }

    public boolean hasWhiteKingOnly() {
        return getWhiteWithoutKing() == 0;
    }

    public boolean whiteHasOnlyOneMinorPiece() {
        int knights = getKnightLocations(PlayerColor.WHITE).length;
        int bishops = getBishopLocations(PlayerColor.WHITE).length;

        return
            (this.boards[Piece.WHITE_PAWN.getIndex()] == 0) &&
            (this.boards[Piece.WHITE_ROOK.getIndex()] == 0) &&
            (this.boards[Piece.WHITE_QUEEN.getIndex()] == 0) &&
            (knights + bishops == 1);
    }

    public boolean whiteHasAllOriginalPieces() {
        return getPawnLocations(PlayerColor.WHITE).length == 8 &&
            getRookLocations(PlayerColor.WHITE).length == 2 &&
            getKnightLocations(PlayerColor.WHITE).length == 2 &&
            getBishopLocations(PlayerColor.WHITE).length == 2 &&
            getQueenLocations(PlayerColor.WHITE).length == 1;
    }

    public boolean whiteHasOnlyTwoKnights() {
        int knights = getKnightLocations(PlayerColor.WHITE).length;

        return
            (this.boards[Piece.WHITE_PAWN.getIndex()] == 0) &&
                (this.boards[Piece.WHITE_BISHOP.getIndex()] == 0) &&
                (this.boards[Piece.WHITE_ROOK.getIndex()] == 0) &&
                (this.boards[Piece.WHITE_QUEEN.getIndex()] == 0) &&
                (knights == 2);
    }

    // ------------------------ Get Pieces methods -------------------------------
    public long getPieceLocations(Piece p) {
        return this.boards[p.getIndex()];
    }

    public long getPawns(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_PAWN.getIndex()] : this.boards[Piece.BLACK_PAWN.getIndex()];
    }

    public long getRooks(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_ROOK.getIndex()] : this.boards[Piece.BLACK_ROOK.getIndex()];
    }

    public long getKnights(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_KNIGHT.getIndex()] : this.boards[Piece.BLACK_KNIGHT.getIndex()];
    }

    public long getBishops(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_BISHOP.getIndex()] : this.boards[Piece.BLACK_BISHOP.getIndex()];
    }

    public long getQueens(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_QUEEN.getIndex()] : this.boards[Piece.BLACK_QUEEN.getIndex()];
    }

    public long getKings(PlayerColor playerColor) {
        return playerColor == PlayerColor.WHITE ? this.boards[Piece.WHITE_KING.getIndex()] : this.boards[Piece.BLACK_KING.getIndex()];
    }

    public int[] getPawnLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_PAWN, Piece.BLACK_PAWN, playerColor);
    }

    public int[] getRookLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_ROOK, Piece.BLACK_ROOK, playerColor);
    }

	public int[] getKnightLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_KNIGHT, Piece.BLACK_KNIGHT, playerColor);
	}

	public int[] getBishopLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_BISHOP, Piece.BLACK_BISHOP, playerColor);
	}

	public int[] getQueenLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_QUEEN, Piece.BLACK_QUEEN, playerColor);
	}

	public int[] getKingLocations(PlayerColor playerColor) {
        return getPieceLocationsAsArray(Piece.WHITE_KING, Piece.BLACK_KING, playerColor);
	}

    public int getKingLocation(PlayerColor color) {
        int index = (color == PlayerColor.WHITE) ? Piece.WHITE_KING.getIndex() : Piece.BLACK_KING.getIndex();
        if(this.boards[index] == 0) // the king has been removed from the board.  This can happen when looking for check
            return -1;
        return PieceMoves.bitscanForward(this.boards[index]);
    }

    public int[] getPieceLocationsAsArray(Piece one, Piece two, PlayerColor activePlayerColor) {
        return Board.bitboardToArray(getPieceLocations(activePlayerColor == PlayerColor.WHITE ? one : two ));
	}

    public boolean hasInsufficientMaterials(boolean isOutOfTime) {
        // logic assumes there is always one black and white king.
        // King vs king
        if(hasBlackKingOnly() && hasWhiteKingOnly())
            return true;

        // King + minor piece (knight of bishop) vs king
        if((hasWhiteKingOnly() && blackHasOnlyOneMinorPiece()) ||
            (hasBlackKingOnly() && whiteHasOnlyOneMinorPiece()))
            return true;

        // King + minor piece vs king + minor piece
        if(blackHasOnlyOneMinorPiece() && whiteHasOnlyOneMinorPiece())
            return true;

        // King + two knights vs king
        if((hasBlackKingOnly() && whiteHasOnlyTwoKnights()) ||
            (hasWhiteKingOnly() && blackHasOnlyTwoKnights()))
            return true;

        // Lone king vs all the pieces & time runs out
        return isOutOfTime &&
            ((hasBlackKingOnly() && whiteHasAllOriginalPieces()) ||
                (hasWhiteKingOnly() && blackHasAllOriginalPieces()));
    }

    // ------------------------ Caching methods -------------------------------
    private void populatePieceCache() {
        Arrays.fill(pieceCache, null);

        for(int i = 0; i < this.boards.length; i++) {
            int[] locations = Board.bitboardToArray(this.boards[i]);
            Piece p = Piece.getPieceByIndex(i);

            for (int location : locations) {
                pieceCache[location] = p;
            }
        }

        for(int k = 0; k < pieceCache.length; k++) {
            if(pieceCache[k] == null)
                pieceCache[k] = Piece.EMPTY;
        }
    }

    public void calculateAllCacheBoards() {
        calculateWhiteBoardWithoutKing();
        calculateWhiteBoard();
        calculateBlackBoardWithoutKing();
        calculateBlackBoard();
        calculateOccupied();
    }

    public long[] copyOfBoards() {
        return getBoards().clone();  // array of primitives will be cloned.
    }

    public Piece[] copyOfPieceCache() {
        return Arrays.copyOf(pieceCache, pieceCache.length);
    }

    public Piece[] getPieceCache() {
        return pieceCache;
    }

    public void setPieceCache(Piece[] pieceCache) {
        this.pieceCache = pieceCache;
    }

    public long getWhite() {
        return this.whiteBoard;
    }

    public long getWhiteWithoutKing() {
        return this.whiteBoardWithoutKing;
    }

    public void calculateWhiteBoard() {
        this.whiteBoard = this.whiteBoardWithoutKing |
            this.boards[Piece.WHITE_KING.getIndex()];
    }

    public void calculateWhiteBoardWithoutKing() {
        this.whiteBoardWithoutKing = this.boards[Piece.WHITE_PAWN.getIndex()] |
            this.boards[Piece.WHITE_KNIGHT.getIndex()] |
            this.boards[Piece.WHITE_BISHOP.getIndex()] |
            this.boards[Piece.WHITE_ROOK.getIndex()] |
            this.boards[Piece.WHITE_QUEEN.getIndex()];
    }

    public long getBlack() {
        return this.blackBoard;
    }

    private void calculateBlackBoard() {
        this.blackBoard = this.blackBoardWithoutKing |
            this.boards[Piece.BLACK_KING.getIndex()];
    }

    public long getBlackWithoutKing() {
        return this.blackBoardWithoutKing;
    }

    private void calculateBlackBoardWithoutKing() {
        this.blackBoardWithoutKing = this.boards[Piece.BLACK_PAWN.getIndex()] |
            this.boards[Piece.BLACK_KNIGHT.getIndex()] |
            this.boards[Piece.BLACK_BISHOP.getIndex()] |
            this.boards[Piece.BLACK_ROOK.getIndex()] |
            this.boards[Piece.BLACK_QUEEN.getIndex()];
    }

    // ------------------------ Castling Rights -------------------------------
    public void setCastlingRights(int castlingRights) {
        this.castlingRights = castlingRights;
    }

    public int getCastlingRights() {
        return this.castlingRights;
    }

    private void clearCastling(int value) {
        updateCastlingRights();
        castlingRights &= ~value;
        updateCastlingRights();
    }

    public boolean canCastlingWhiteKingSide()  { return (castlingRights & CastlingHelper.CASTLING_WHITE_KING_SIDE) != 0; }
    public boolean canCastlingBlackKingSide()  { return (castlingRights & CastlingHelper.CASTLING_BLACK_KING_SIDE) != 0; }
    public boolean canCastlingWhiteQueenSide() { return (castlingRights & CastlingHelper.CASTLING_WHITE_QUEEN_SIDE) != 0; }
    public boolean canCastlingBlackQueenSide() { return (castlingRights & CastlingHelper.CASTLING_BLACK_QUEEN_SIDE) != 0; }

    public boolean cannotCastle()              { return castlingRights == 0; }
    public void clearCastlingWhiteKingSide()  { clearCastling(CastlingHelper.CASTLING_WHITE_KING_SIDE ); }
    public void clearCastlingBlackKingSide()  { clearCastling(CastlingHelper.CASTLING_BLACK_KING_SIDE ); }
    public void clearCastlingWhiteQueenSide() { clearCastling(CastlingHelper.CASTLING_WHITE_QUEEN_SIDE); }
    public void clearCastlingBlackQueenSide() { clearCastling(CastlingHelper.CASTLING_BLACK_QUEEN_SIDE); }
    public void clearCastlingWhite()          { clearCastling((CastlingHelper.CASTLING_WHITE_KING_SIDE | CastlingHelper.CASTLING_WHITE_QUEEN_SIDE)); }

    public void initializeCastlingRights() {
        castlingRights =
            CastlingHelper.CASTLING_WHITE_KING_SIDE |
                CastlingHelper.CASTLING_WHITE_QUEEN_SIDE |
                CastlingHelper.CASTLING_BLACK_KING_SIDE |
                CastlingHelper.CASTLING_BLACK_QUEEN_SIDE;
        zobristHasher.updateCastlingRights(0, true, true, true, true);
    }

    public void setCastlingRightsFromFen(String fen) {
        // if(fen.isBlank()) { clearCastlingRights(); }
        if(fen.indexOf(CastlingHelper.WHITE_KING_CHAR) >= 0)  { addCastlingRights(CastlingHelper.CASTLING_WHITE_KING_SIDE ); }
        if(fen.indexOf(CastlingHelper.WHITE_QUEEN_CHAR) >= 0) { addCastlingRights(CastlingHelper.CASTLING_WHITE_QUEEN_SIDE); }
        if(fen.indexOf(CastlingHelper.BLACK_KING_CHAR) >= 0)  { addCastlingRights(CastlingHelper.CASTLING_BLACK_KING_SIDE ); }
        if(fen.indexOf(CastlingHelper.BLACK_QUEEN_CHAR) >= 0) { addCastlingRights(CastlingHelper.CASTLING_BLACK_QUEEN_SIDE); }
    }

    public String getCastlingRightsForFen() {
        if(castlingRights == 0) return "-";

        StringBuilder sb = new StringBuilder();

        if((castlingRights & CastlingHelper.CASTLING_WHITE_KING_SIDE ) != 0) sb.append(CastlingHelper.WHITE_KING_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_WHITE_QUEEN_SIDE) != 0) sb.append(CastlingHelper.WHITE_QUEEN_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_BLACK_KING_SIDE ) != 0) sb.append(CastlingHelper.BLACK_KING_CHAR);
        if((castlingRights & CastlingHelper.CASTLING_BLACK_QUEEN_SIDE) != 0) sb.append(CastlingHelper.BLACK_QUEEN_CHAR);

        return sb.toString();
    }

    public void clearCastlingBlack()          { clearCastling((CastlingHelper.CASTLING_BLACK_KING_SIDE | CastlingHelper.CASTLING_BLACK_QUEEN_SIDE)); }

/*
    public void clearCastlingRights() {
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights off
        castlingRights = 0;
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights on
    }
*/

    public void addCastlingRights(int value) {
        updateCastlingRights();
        castlingRights |= value;
        updateCastlingRights();
    }

    // --------------------------- piece moves and attacks ---------------------------
    public boolean locationUnderAttack(PlayerColor color, int location) {
        if(rookCaptures(color, location) != 0) return true;  // can the opposing player capture the king with their rooks?
        if(bishopCaptures(color, location) != 0) return true;
        if(queenCaptures(color, location) != 0) return true;

        if((pawnCaptures(color.otherColor(), location) & getPawns(color)) != 0) return true;
        if((knightCaptures(location) & getKnights(color)) != 0) return true;
        return (kingCaptures(location) & getKings(color)) != 0;
    }


    public void pawnMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        if(activePlayerColor == PlayerColor.WHITE) {
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
        long twoStep = ((oneStep & Board.RANK_3) << 8L) & ~getOccupied();
        int diff = -16;

        for (int dest : Board.bitboardToArray(twoStep)) {
            Move m = new Move(Piece.WHITE_PAWN, dest + diff, dest, Move.NON_CAPTURE);
            long enPassantMask = ((1L << (dest + 1)) | (1L << (dest - 1))) & getBlack();

            enPassantMask &= Board.RANK_4;

            if(enPassantMask != 0) {
                m.setEnPassant(dest - 8);
            }

            playerMoves.add(m);
        }
    }

    private void blackTwoStepsForward(MoveList<Move> playerMoves, long oneStep) {
        long twoStep = ((oneStep & Board.RANK_6) >> 8L) & ~getOccupied();
        int diff = 16;

        for (int dest : Board.bitboardToArray(twoStep)) {
            Move m = new Move(Piece.BLACK_PAWN, dest + diff, dest, Move.NON_CAPTURE);
            long enPassantMask = ((1L << (dest + 1)) | (1L << (dest - 1))) & getWhite();

            enPassantMask &= Board.RANK_5;

            if(enPassantMask != 0) {
                m.setEnPassant(dest + 8);
            }

            playerMoves.add(m);
        }
    }

    private long whitePawnOneStep(MoveList<Move> playerMoves) {
        long oneStep = (getWhitePawnBoard() << 8L) & ~getOccupied();
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
        long oneStep = (getBlackPawnBoard() >> 8L) & ~getOccupied();
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
        long pawns = getBlackPawnBoard();
        long opponentBoard = getWhite();
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
        long pawns = getWhitePawnBoard();
        long opponentBoard = getBlack();
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

    //   8/K1b5/1k5p/7P/8/8/8/8 b - - 0 1
    //   K7/2b5/1k5p/7P/8/8/8/8 w - -

    public void kingMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int kingLocation = getKingLocation(activePlayerColor);
        KingMoves rawMoves = new KingMoves(this, kingLocation);
        Piece king = get(kingLocation);
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
            to == CastlingHelper.WHITE_KING_SIDE_CASTLING_KING_LOCATION &&
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
    }

    public void queenMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getQueenLocations(activePlayerColor);

        for(int i : locations) {
            QueenMoves rawMoves = new QueenMoves(this, i);
            Piece piece = get(i);

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

    public void bishopMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getBishopLocations(activePlayerColor);

        for(int i : locations) {
            BishopMoves rawMoves = new BishopMoves(this, i);
            Piece piece = get(i);

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

    public void knightMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getKnightLocations(activePlayerColor);

        for(int i : locations) {
            KnightMoves rawMoves = new KnightMoves(this, i);
            Piece piece = get(i);

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

    public void rookMoves(MoveList<Move> playerMoves, PlayerColor activePlayerColor) {
        int[] locations;
        locations = getRookLocations(activePlayerColor);

        for(int i : locations) {
            RookMoves rawMoves = new RookMoves(this, i);
            Piece piece = get(i);

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
        long rooks = getRooks(pc);
        long rooksAttacks = getRookAttacksForSquare(targetLocation);
        return rooksAttacks & rooks;
    }

    private long getRookAttacksForSquare(int location) {
        return StraightLineMoves.getRookAttacks(location, getOccupied()) & ~0L;
    }

    private long bishopCaptures(PlayerColor pc, int targetLocation) {
        long bishops = getBishops(pc);
        long bishopAttacks = getBishopAttacksForSquare(targetLocation);
        return bishopAttacks & bishops;
    }

    private long queenCaptures(PlayerColor pc, int targetLocation) {
        long queens = getQueens(pc);
        long rooksAttacks = getRookAttacksForSquare(targetLocation);
        long bishopAttacks = getBishopAttacksForSquare(targetLocation);
        return (rooksAttacks | bishopAttacks) & queens;
    }

    private long getBishopAttacksForSquare(int location) {
        return StraightLineMoves.getBishopAttacks(location, getOccupied()) & ~0L;
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


    // --------------------------- Visualization ---------------------------

    // implement a Forsyth-Edwards toString() method
    public String toFen() {
        StringBuilder f = new StringBuilder(100);

        int rank = 7;
        int location = 56;
        int max;
        int n;
        Piece p;

        while (rank >= 0) {
            p = get(location);

            if (p == Piece.EMPTY) {
                max = 8 - file(location);
                n = this.nextPiece(location, max);
                f.append(n - location); // use a number to show how many spaces are left
            } else {
                f.append(p);
                n = location + 1;
            }

            location = n;

            if (file(location) == 0) {
                if (rank > 0) {
                    f.append('/');
                }
                rank--;
                location = rank * 8;
            }
        }

        return f.toString();
    }
    // Note: this isn't called very often, so not worth optimizing

    protected int nextPiece(int location, int max) {
        int i = 1;

        while (location + i < 64 &&
            i < max &&
            this.get(location + i) == Piece.EMPTY)
            i++;

        return i + location;
    }

    // ------------------------ Zobrist Keys -------------------------------

    /**
     * Returns a Zobrist hash code value for this board. A Zobrist hashing assures the same position returns the same
     * hash value. It is calculated using the position of the pieces, the side to move, the castle rights and the en
     * passant target.
     *
     * @return a Zobrist hash value for this board
     * @see <a href="https://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing in Wikipedia</a>
     */
    public long getZobristKey() {
        return zobristHasher.getZobristKey();
    }

    public void initializeZobristKey(PlayerColor pc) {
        zobristHasher = new ZobristHasher(pc, this);
    }

    public long resetZobristKey(PlayerColor pc) {
        return zobristHasher.reset(pc, this);
    }

    public void updateZobristKeyFlipPlayer(PlayerColor c) {
        zobristHasher.updateZobristKeyFlipPlayer(c);
    }

    public void updateCastlingRights() {
        zobristHasher.updateCastlingRights(
            castlingRights,
            canCastlingWhiteKingSide(),
            canCastlingWhiteQueenSide(),
            canCastlingBlackKingSide(),
            canCastlingBlackQueenSide());
    }

    // --------------------------- En Passant ---------------------------

    void setEnPassantTargetFromFen(String target) {
        if(target.equals(PawnMoves.NO_EN_PASSANT))
            clearEnPassantTarget();
        else
            setEnPassantTarget(FenString.squareToLocation(target));
    }

    void clearEnPassantTarget() {
        setEnPassantTarget(PawnMoves.NO_EN_PASSANT_VALUE);
    }

    void setEnPassantTarget(int target) {
        if(target == getEnPassantTarget())
            return;

        if(getEnPassantTarget() != PawnMoves.NO_EN_PASSANT_VALUE)
            zobristHasher.updateZobristKeyWithEnPassant(getEnPassantTarget());  // clear the current value if set

        enPassantTarget = target;
        if(enPassantTarget != PawnMoves.NO_EN_PASSANT_VALUE)
            zobristHasher.updateZobristKeyWithEnPassant(enPassantTarget); // add the new value
    }

    public String getEnPassantTargetAsFen() {
        if (enPassantTarget == -1) {
            return "-";
        }

        return FenString.locationToSquare(enPassantTarget);
    }

    public int getEnPassantTarget() {
        return enPassantTarget;
    }

    public boolean hasEnPassantTarget() { return enPassantTarget != PawnMoves.NO_EN_PASSANT_VALUE; }

}
