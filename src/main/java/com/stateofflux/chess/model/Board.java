package com.stateofflux.chess.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stateofflux.chess.model.pieces.CastlingHelper;
import com.stateofflux.chess.model.pieces.PieceMoves;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stateofflux.chess.model.pieces.Piece;

public class Board {
    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    public static long RANK_1 = 0xffL;
    public static long RANK_2 = 0xff00L;
    public static long RANK_3 = 0xff0000L;
    public static long RANK_4 = 0xff000000L;
    public static long RANK_5 = 0xff00000000L;
    public static long RANK_6 = 0xff0000000000L;
    public static long RANK_7 = 0xff000000000000L;
    public static long RANK_8 = 0xff00000000000000L;

    public static long FILE_H = 0x8080808080808080L;
    public static long FILE_G = 0x4040404040404040L;
    public static long FILE_F = 0x2020202020202020L;
    public static long FILE_E = 0x1010101010101010L;
    public static long FILE_D = 0x808080808080808L;
    public static long FILE_C = 0x404040404040404L;
    public static long FILE_B = 0x202020202020202L;
    public static long FILE_A = 0x101010101010101L;

    private long[] boards = new long[Piece.SIZE];

    // caches
    private long occupiedBoard;
    private long blackBoard;
    private long whiteBoard;
    private long blackBoardWithoutKing;
    private long whiteBoardWithoutKing;
    private Piece[] pieceCache = new Piece[64];

    // instance vars
    protected int castlingRights;
    private long zobristKey;

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
    }

    /*
     * Build a board using a fen string
     */
    public Board(String fen) {
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

        // todo - short circuit the loop if the
        for (int i = 0; i < 64 && counter < bitsSet; i++) {
            if ((l & (1L << i)) != 0) {
                result[counter++] = i;
            }
        }

        return result;
    }

    // --------------------------- Instance Methods ---------------------------

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

    public long setByBoard(Piece piece, int boardIndex, int location) {
        long temp = this.boards[boardIndex] |= (1L << location);
        this.updateZobristKeyWhenSetting(piece, location);
        pieceCache[location] = Piece.getPieceByIndex(boardIndex);
        return temp;
    }

    public void clearByBoard(Piece piece, int boardIndex, int location) {
        this.boards[boardIndex] ^= (1L << location);  // TODO: should be this.boards[boardIndex] &= ~(1L << location);  ??
        updateZobristKeyWhenSetting(piece, location);
        pieceCache[location] = Piece.EMPTY;

        if(boardIndex <= 5) { // white
            calculateWhiteBoard();
            calculateWhiteBoardWithoutKing();
        } else if(boardIndex <= 11) { // black
            calculateBlackBoard();
            calculateBlackBoardWithoutKing();
        }

        calculateOccupied();
    }

    protected void clearLocation(int location) {
        long bitToClear = 1L << location;

        for(int i = 0; i < boards.length; i++) {
            if((boards[i] & bitToClear) != 0) {
                // clear the bit at the location on all boards
                clearZorbistMask(bitToClear);
                boards[i] &= ~bitToClear;
            }
        }
    }

    public int set(char element, int location) {
        for (Piece piece : Piece.values()) {
            if (element == piece.getPieceChar()) {
                set(piece, location);
                location++;
                break;
            }
        }
        return location;
    }

    public long set(Piece piece, int location) {
        if (piece == Piece.EMPTY)
            throw new IllegalArgumentException("Cannot place empty piece");

        return setByBoard(piece, piece.getIndex(), location);
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
    /*
     * Move a piece on the board, but do not perform validation.
     * return the removed location
     */

    public int update(Move m, int gameEnPassantState) {
        boolean standardMove = true;
        int removed = -1;

        int fromBoardIndex = this.getBoardIndex(m.getFrom());  // TODO: replace fromBoardIndex = m.getPiece().getIndex();
        clearByBoard(m.getPiece(), fromBoardIndex, m.getFrom()); // clear
        removed = m.getTo();  // TODO: replace with pieceCache[m.getTo()].getIndex();
        this.clearLocation(m.getTo());  // take the destination piece off the board if it exists.  It may be on any bitboard

        // if promotion
        if(m.isPromoting()) {
            fromBoardIndex = m.getPromotionPiece().getIndex();
            setByBoard(m.getPiece(), fromBoardIndex, m.getTo()); // set promoted piece
            standardMove = false;
        }

        if(m.isEnPassant()) {
            int target = m.getEnPassantTarget();
            // boardIndex doesn't change (pawn to pawn)
            removed = target;
            this.clearLocation(target);  // clear en passant target - TODO: this is not the right time to do this so delete this line.
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

        return removed;
    }

    public long getOccupied() {
        return this.occupiedBoard;
    }

    public long calculateOccupied() {
        long usedBoard = 0L;

        for (long board : boards) {
            usedBoard |= board;
        }

        return this.occupiedBoard = usedBoard;
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

    public long getWhitePawns() {
        return this.boards[Piece.WHITE_PAWN.getIndex()];
    }

    public long getBlackPawns() {
        return this.boards[Piece.BLACK_PAWN.getIndex()];
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

    public long[] getCopyOfBoards() {
        return getBoards().clone();  // array of primitives will be cloned.
    }

    public Piece[] getCopyOfPieceCache() {
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
        this.zobristKey ^= getCastlingRights(castlingRights);  // xor the castling rights off
        castlingRights &= ~value;
        this.zobristKey ^= getCastlingRights(castlingRights);  // xor the castling rights on
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

    public void setInitialCastlingRights() {
        castlingRights =
            CastlingHelper.CASTLING_WHITE_KING_SIDE |
                CastlingHelper.CASTLING_WHITE_QUEEN_SIDE |
                CastlingHelper.CASTLING_BLACK_KING_SIDE |
                CastlingHelper.CASTLING_BLACK_QUEEN_SIDE;
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights off
    }

    public void setCastlingRightsFromFen(String fen) {
        if(fen.isBlank()) { clearCastlingRights(); }
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

    public void clearCastlingRights() {
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights off
        castlingRights = 0;
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights on
    }

    public void addCastlingRights(int value) {
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights off
        castlingRights |= value;
        this.zobristKey ^= getCastlingRights(this.zobristKey);  // xor the castling rights on
    }

    // --------------------------- Visualization ---------------------------

    // implement a Forsyth-Edwards toString() method
    public String toFen() {
        StringBuilder f = new StringBuilder(100); // TODO - initialize with size.

        int rank = 7;
        int location = 56;
        int max = 0;
        int n = 0;
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

    // See https://en.wikipedia.org/wiki/Zobrist_hashing for overview and technique for incremental updates.
    // from https://github.com/bhlangonijr/chesslib/blob/49599909c02fc652b15d89048ec88f8b707facf6/src/main/java/com/github/bhlangonijr/chesslib/Board.java
    private static final long RANDOM_SEED = 49109794719L;
    private static final int ZOBRIST_TABLE_SIZE = 2000;
    private static final List<Long> zorbistRandomKeys = new ArrayList<>(ZOBRIST_TABLE_SIZE);

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
        return this.zobristKey;
    }

    public void setZobristKey(PlayerColor color, int epT) {
        this.zobristKey = calculateFullZorbistKey(color, epT);
    }

    private long calculateFullZorbistKey(PlayerColor color, int epT) {
        //        PlayerColor color = getActivePlayerColor();
//        LOGGER.info("zobrist: {}, {}, {}, {}", asFen(), castlingRights, color, getEnPassantTargetAsInt());
        long hash = 0;

        hash = getCastlingRights(hash);

        // TODO: I think it will be faster to iterate over each board's bits so replace this algo.

        for (int i = 0; i < 64; i++) {
            Piece piece = get(i);
            if (piece != Piece.EMPTY)
                hash ^= getPieceSquareKey(piece, i);
        }

        hash ^= getSideKey(color);

        if (epT >= 0) { hash ^= getEnPassantKey(epT); }
        return hash;
    }

    public void updateZobristKeyWithEnPassant(int epT) {
        this.zobristKey ^= getEnPassantKey(epT);
    }

    private long getCastlingRights(long hash) {
        if(canCastlingWhiteKingSide())  hash ^= getCastleRightKey(1, PlayerColor.WHITE);
        if(canCastlingWhiteQueenSide()) hash ^= getCastleRightKey(2, PlayerColor.WHITE);
        if(canCastlingBlackKingSide())  hash ^= getCastleRightKey(3, PlayerColor.BLACK);
        if(canCastlingBlackQueenSide()) hash ^= getCastleRightKey(4, PlayerColor.BLACK);
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

    public void clearZorbistMask(long mask) {
        this.zobristKey &= ~mask;
    }

    public void updateZobristKeyWhenSetting(Piece piece, int square) {
        this.zobristKey ^= getPieceSquareKey(piece, square);
    }

    public void updateZobristKeyFlipPlayer(PlayerColor c) {
        this.zobristKey ^= getSideKey(c);
    }

}
