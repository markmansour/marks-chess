package com.stateofflux.chess.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stateofflux.chess.model.pieces.Piece;

/*
 * Class uses Forsyth–Edwards Notation
 *
 * https://en.wikipedia.org/wiki/Forsyth–Edwards_Notation
 *
 * https://en.wikipedia.org/wiki/Shannon_number describes the number of possible games.
 * For a depth of 10 (known as ply) there are 69 trillion possible games.
 *
 * Note: Assume that each game needs a copy of this board.
 *
 * There are 11 longs (64 bits / 8 bytes each) to represent the board, a total of 88 bytes (11 * 8 bytes) plus object overhead.
 *
 * Therefore 88 bytes * 69 trillion games is 69,000,000,000,000/1024/1024/1024
 *   => 64,261 Gigabytes of memory.  This assumes no pruning of branches that would be a low yield.
 *
 * Useful links
 * * https://en.wikipedia.org/wiki/Portal:Chess
 * * https://chessify.me/blog/what-is-depth-in-chess-different-depths-for-stockfish-and-lczero
 */
public class Board {
    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    private long[] boards = new long[Piece.SIZE];
    private Game game;
    private final Piece[] pieceByLocationCache = new Piece[64];

    // cached state
    private long occupiedBoard;
    private long blackBoard;
    private long whiteBoard;
    private long blackBoardWithoutKing;
    private long whiteBoardWithoutKing;

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

        preWarmCache();
        calculateAllCacheBoards();
    }

    // --------------------------- Constructors ---------------------------
    /*
     * Build a board using a fen string
     */
    public Board(String fen) {
        this.populate(fen);
        preWarmCache();
        calculateAllCacheBoards();
    }

    public void calculateAllCacheBoards() {
        calculateWhiteBoardWithoutKing();
        calculateWhiteBoard();
        calculateBlackBoardWithoutKing();
        calculateBlackBoard();
        calculateOccupied();
    }
    private void preWarmCache() {
        // warming of the cache doesn't have a measurable impact on speed
/*
        for(int i = 0; i < 64; i++) {
            get(i);
        }
*/
    }

    public void setBoards(long[] boards) {
        this.boards = Arrays.copyOf(boards, boards.length);
        calculateAllCacheBoards();
    }

    public long[] getBoards() {
        return boards;
    }

    public void populate(String fen) {
        char[] fenCh = fen.toCharArray();
        int rank = 7;
        int location = 56;

        for (char element : fenCh) {
            if (element == '/') {
                if (location % 8 != 0)
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

    // --------------------------- Instance Methods ---------------------------
    public long setByBoard(int boardIndex, int location) {
        long temp = this.boards[boardIndex] |= (1L << location);
        return temp;
    }

    public void clearByBoard(int boardIndex, int location) {
        this.boards[boardIndex] ^= (1L << location);

        if(boardIndex <= 5) { // white
            calculateWhiteBoard();
            calculateWhiteBoardWithoutKing();
        } else if(boardIndex <= 11) { // black
            calculateBlackBoard();
            calculateBlackBoardWithoutKing();
        }

        calculateOccupied();
    }

    // TODO: Can I remove this method to make it more efficient?
    protected void clearLocation(int location) {
        for(int i = 0; i < this.boards.length; i++) {
            // clear the bit at the location on all boards
            this.boards[i] &= ~(1L << location);
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

        return setByBoard(piece.getIndex(), location);
    }

    public void remove(int location) {
        long bitLocation = 1L << location;

        for (int i = 0; i < this.boards.length; i++) {
            if ((this.boards[i] & bitLocation) != 0)
                clearByBoard(i, location);
        }
    }

    /*
     * return the character representing a piece
     */
    public Piece get(int location) {
        long bitLocation = 1L << location;
        Piece cachedPiece = pieceByLocationCache[location];

        // cache hit
        if(cachedPiece != null && (this.boards[cachedPiece.getIndex()] & bitLocation) != 0)
            return cachedPiece;

        // cache miss
        for (int i = 0; i < this.boards.length; i++) {
            if ((this.boards[i] & bitLocation) != 0) {
                Piece p = Piece.getPieceByIndex(i);
                pieceByLocationCache[location] = p;
                return p;
            }
        }

        return Piece.EMPTY;
    }

    protected int getBitSetIndex(int location) {
        for (int boardCount = 0; boardCount < this.boards.length; boardCount++) {
            if ((this.boards[boardCount] & (1L << location)) != 0)
                return boardCount;
        }

        throw new AssertionError("Location not found: " + this);
    }

    // starting from location, but not looking ahead more than max, find the next
    // piece on the board
    // TODO: Could this be done as a BitSet operation?
    protected int nextPiece(int location, int max) {
        int i = 1;

        while (location + i < 64 &&
                i < max &&
                this.get(location + i) == Piece.EMPTY)
            i++;

        return i + location;
    }

    /*
     * Move a piece on the board, but do not perform validation.
     * return the removed location
     */
    public int update(Move m, int gameEnPassantState) {
        boolean standardMove = true;
        int removed = -1;

        int fromBoardIndex = this.getBitSetIndex(m.getFrom());
        clearByBoard(fromBoardIndex, m.getFrom()); // clear
        removed = m.getTo();
        this.clearLocation(m.getTo());  // take the destination piece off the board if it exists.  It may be on any bitboard

        // if promotion
        if(m.isPromoting()) {
            fromBoardIndex = m.getPromotionPiece().getIndex();
            setByBoard(fromBoardIndex, m.getTo()); // set promoted piece
            standardMove = false;
        }

        if(m.isEnPassant()) {
            int target = FenString.squareToLocation(m.getEnPassantTarget());
            // boardIndex doesn't change (pawn to pawn)
            removed = target;
            this.clearLocation(target);  // clear en passant target
            setByBoard(fromBoardIndex, m.getTo()); // set new pawn location
            standardMove = false;
        }

        // remove the piece that created the en passant scenario.

        if(m.isEnPassantCapture(gameEnPassantState)) {
            int target = m.getTo();

            int sourceFile, destFile;
            sourceFile = m.getFrom() % 8;
            destFile = target % 8;

            assert(sourceFile != destFile);
            if(sourceFile < destFile)
                removed = m.getFrom() + 1;
            else if(sourceFile > destFile)
                removed = m.getFrom() - 1;

            this.clearLocation(removed);  // clear en passant target

            // boardIndex doesn't change (pawn to pawn)
            setByBoard(fromBoardIndex, m.getTo()); // set new pawn location
            standardMove = false;
        }

        // if castling - king already moved
        if(m.isCastling()) {
            setByBoard(fromBoardIndex, m.getTo()); // set king location

            int toBoardIndex = this.getBitSetIndex(m.getSecondaryFrom());
            clearByBoard(toBoardIndex, m.getSecondaryFrom()); // clear
            setByBoard(toBoardIndex, m.getSecondaryTo()); // set rook location

            standardMove = false;
        }

        if(standardMove) {
            setByBoard(fromBoardIndex, m.getTo()); // set
        }

        calculateAllCacheBoards();

        return removed;
    }

    public boolean isEmpty(int location) {
        return (~this.getOccupied() & (1L << location)) != 0;
    }

    // this can be optimized.
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

    public long getBlack() {
        return this.blackBoard;
    }

    private long calculateBlackBoard() {
        return this.blackBoard = this.blackBoardWithoutKing |
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

    public long getWhite() {
        return this.whiteBoard;
    }

    public long calculateWhiteBoard() {
        return this.whiteBoard = this.whiteBoardWithoutKing |
            this.boards[Piece.WHITE_KING.getIndex()];
    }

    public long getWhiteWithoutKing() {
        return this.whiteBoardWithoutKing;
    }

    public long calculateWhiteBoardWithoutKing() {
        return this.whiteBoardWithoutKing = this.boards[Piece.WHITE_PAWN.getIndex()] |
            this.boards[Piece.WHITE_KNIGHT.getIndex()] |
            this.boards[Piece.WHITE_BISHOP.getIndex()] |
            this.boards[Piece.WHITE_ROOK.getIndex()] |
            this.boards[Piece.WHITE_QUEEN.getIndex()];
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

    public long getWhitePawns() {
        return this.boards[Piece.WHITE_PAWN.getIndex()];
    }

    public long getBlackPawns() {
        return this.boards[Piece.BLACK_PAWN.getIndex()];
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
                max = 8 - (location % 8);
                n = this.nextPiece(location, max);
                f.append(n - location); // use a number to show how many spaces are left
            } else {
                f.append(p);
                n = location + 1;
            }

            location = n;

            if (location % 8 == 0) {
                if (rank > 0) {
                    f.append('/');
                }
                rank--;
                location = rank * 8;
            }
        }

        return f.toString();
    }

    public void printOccupied() {
        StringBuilder prettyBoard = new StringBuilder(64);

        for (int i = 0; i < 64; i++) {
            prettyBoard.insert(i, get(i));
        }

        CharSequence[] ranks = new CharSequence[8];

        for (int i = 7; i >= 0; i--) {
            ranks[i] = prettyBoard.subSequence(i * 8, (i + 1) * 8);
            LOGGER.info("{}: {}", Integer.valueOf(i + 1), ranks[i]);
        }

        LOGGER.info("   abcdefgh");

        var game = this.getGame();
        if(game != null) {
            LOGGER.info("FEN: {}", game.asFen());
            LOGGER.info("isOver: {}", game.isOver());
            LOGGER.info("isCheckmated: {}", game.isCheckmated(game.activePlayerColor));
            LOGGER.info("hasResigned: {}", game.hasResigned());
            LOGGER.info("isStalemate: {}", game.isStalemate());
            LOGGER.info("hasInsufficientMaterials: {}", game.hasInsufficientMaterials());
            LOGGER.info("exceededMoves: {}", game.exceededMoves());
            LOGGER.info("hasRepeated: {}", game.isRepetition());
        }
        LOGGER.info("--------------------------");
    }

    // --------------------------- Static Methods ---------------------------
    // useful for debugging.
    public static void printOccupied(long board) {
        StringBuilder rankString;
        List<String> ranks = new ArrayList<>();

        for (int rank = 8; rank > 0; rank--) {
            rankString = new StringBuilder(8);
            for (int location = (rank - 1) * 8; location < rank * 8; location++) {
                rankString.append((board & (1L << location)) == 0 ? '.' : '1');
            }

            ranks.add(rankString.toString());
        }

        int i = 0;
        for (String r : ranks) {
            LOGGER.info("{}: {}", 8 - i, r);
            i++;
        }

        LOGGER.info("   abcdefgh");
    }

    public static void printBoard(long board) {
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

    public int[] getPieceLocationsAsArray(Piece one, Piece two, PlayerColor activePlayerColor) {
        return Board.bitboardToArray(getPieceLocations(activePlayerColor == PlayerColor.WHITE ? one : two ));
	}

    public int getWhiteKingLocation() {
        return bitboardToArray(this.boards[Piece.WHITE_KING.getIndex()])[0];
    }

    public int getBlackKingLocation() {
        return bitboardToArray(this.boards[Piece.BLACK_KING.getIndex()])[0];
    }

    // TODO - still buggy
    // TEST DATA:
    // * FEN: r3Q1N1/2B3r1/P3b1p1/1pp4p/1R5P/1kR5/7N/4K3 w - - 0 97
    // * move: R : c3b3
    // results in a 0 length array.
    public int getKingLocation(PlayerColor color) {
        int index = (color == PlayerColor.WHITE) ? Piece.WHITE_KING.getIndex() : Piece.BLACK_KING.getIndex();
        if(this.boards[index] == 0) // the king has been removed from the board.  This can happen when looking for check
            return -1;

        return bitboardToArray(this.boards[index])[0];
    }

    public long getPieceLocations(Piece p) {
        return this.boards[p.getIndex()];
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

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return this.game;
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
        if(isOutOfTime &&
            ((hasBlackKingOnly() && whiteHasAllOriginalPieces()) ||
                (hasWhiteKingOnly() && blackHasAllOriginalPieces())))
            return true;

        return false;
    }

    public long[] getCopyOfBoards() {
        return Arrays.copyOf(getBoards(), getBoards().length);
    }
}
