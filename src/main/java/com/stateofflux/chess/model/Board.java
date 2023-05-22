package com.stateofflux.chess.model;

import java.util.ArrayList;
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

    protected long[] boards = new long[Piece.SIZE];

    /**
     * RANKS:
     * 8 | 56 57 58 59 60 61 62 63 (MSB,
     * 7 | 48 49 50 51 52 53 54 55 left)
     * 6 | 40 41 42 43 44 45 46 47
     * 5 | 32 33 34 35 36 37 38 39
     * 4 | 24 25 26 27 28 29 30 31
     * 3 | 16 17 18 19 20 21 22 23
     * 2 | 8 9 10 11 12 13 14 15
     * 1 | (LSB, 0 1 2 3 4 5 6 7 right)
     * -------------------------------------------
     * FILES: a b c d e f g h
     *
     *
     *
     * 8 ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
     * 7 ♟︎ ♟︎ ♟︎ ♟︎ ♟︎ ♟︎ ♟︎ ♟︎
     * 6
     * 5
     * 4
     * 3
     * 2 ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
     * 1 ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
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
    }

    // --------------------------- Constructors ---------------------------
    /*
     * Build a board using a fen string
     */
    public Board(String fen) {
        FenString.populateBoard(this, fen);
    }

    // --------------------------- Instance Methods ---------------------------
    public int setPieceOnBoard(char element, int location) {
        for (Piece piece : Piece.values()) {
            if (element == piece.getPieceChar()) {
                setPieceOnBoard(piece, location);
                location++;
                break;
            }
        }
        return location;
    }

    public long setPieceOnBoard(Piece piece, int location) {
        if (piece == Piece.EMPTY)
            throw new IllegalArgumentException("Cannot place empty piece");

        return this.boards[piece.getIndex()] |= 1L << location;
    }

    public void removePieceFromBoard(int location) {
        long bitLocation = 1L << location;

        for (int i = 0; i < this.boards.length; i++) {
            if ((this.boards[i] & bitLocation) != 0)
                this.boards[i] ^= bitLocation;
        }
    }

    /*
     * return the characture representing a piece
     */
    public Piece getPieceAtLocation(int location) {
        long bitLocation = 1L << location;

        for (int i = 0; i < this.boards.length; i++) {
            if ((this.boards[i] & bitLocation) != 0)
                return Piece.getPieceByIndex(i);
        }

        return Piece.EMPTY;
    }

    protected int getBitSetIndexAtLocation(int location) {
        for (int boardCount = 0; boardCount < this.boards.length; boardCount++) {
            if ((this.boards[boardCount] & (1L << location)) > 0)
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
                this.getPieceAtLocation(location + i) == Piece.EMPTY)
            i++;

        return i + location;
    }

    /*
     * Move a piece on the board, but do not perform validation.
     */
    public boolean move(String from, String to) {
        int fromIndex = FenString.squareToLocation(from);
        int toIndex = FenString.squareToLocation(to);
        return move(fromIndex, toIndex);
    }

    public boolean move(int fromIndex, int toIndex) {
        // attempting to move from an empty location
        if (fromIndex == -1)
            throw new AssertionError("Source location not found: " + this);

        int boardIndex = this.getBitSetIndexAtLocation(fromIndex);

        this.clearLocation(toIndex);  // take the destination piece off the board if it exists
        this.boards[boardIndex] ^= (1L << fromIndex); // clear
        this.boards[boardIndex] |= (1L << toIndex); // set

        return true;
    }

    public boolean isEmpty(int location) {
        return (~this.getOccupiedBoard() & (1L << location)) != 0;
    }

    // this can be optimized.
    public long getOccupiedBoard() {
        long usedBoard = 0L;

        for (long board : boards) {
            usedBoard |= board;
        }

        return usedBoard;
    }

    public long getBlackBoard() {
        return this.boards[Piece.BLACK_PAWN.getIndex()] |
                this.boards[Piece.BLACK_KNIGHT.getIndex()] |
                this.boards[Piece.BLACK_BISHOP.getIndex()] |
                this.boards[Piece.BLACK_ROOK.getIndex()] |
                this.boards[Piece.BLACK_QUEEN.getIndex()] |
                this.boards[Piece.BLACK_KING.getIndex()];
    }

    public long getWhiteBoard() {
        return this.boards[Piece.WHITE_PAWN.getIndex()] |
                this.boards[Piece.WHITE_KNIGHT.getIndex()] |
                this.boards[Piece.WHITE_BISHOP.getIndex()] |
                this.boards[Piece.WHITE_ROOK.getIndex()] |
                this.boards[Piece.WHITE_QUEEN.getIndex()] |
                this.boards[Piece.WHITE_KING.getIndex()];
    }

    public long getWhitePawnBoard() {
        return this.boards[Piece.WHITE_PAWN.getIndex()];
    }

    public long getBlackPawnBoard() {
        return this.boards[Piece.BLACK_PAWN.getIndex()];
    }

    // --------------------------- Visualization ---------------------------
    // implement a Forsyth-Edwards toString() method
    public String toFenPiecePlacementString() {
        StringBuilder f = new StringBuilder(100); // TODO - initialize with size.

        int rank = 7;
        int location = 56;
        int max = 0;
        int n = 0;
        Piece p;

        while (rank >= 0) {
            p = getPieceAtLocation(location);

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

    public void printBoard() {
        StringBuilder prettyBoard = new StringBuilder(64);

        for (int i = 0; i < 64; i++) {
            prettyBoard.insert(i, getPieceAtLocation(i));
        }

        CharSequence[] ranks = new CharSequence[8];

        for (int i = 7; i >= 0; i--) {
            ranks[i] = prettyBoard.subSequence(i * 8, (i + 1) * 8);
            LOGGER.info("{}: {}", Integer.valueOf(i + 1), ranks[i]);
        }

        LOGGER.info("   abcdefgh");
    }

    // --------------------------- Static Methods ---------------------------
    // useful for debugging.
    public static void printBoard(long board) {
        StringBuilder rankString;
        List<String> ranks = new ArrayList<>();

        for (int rank = 8; rank > 0; rank--) {
            rankString = new StringBuilder(8);
            for (int location = (rank - 1) * 8; location < rank * 8; location++) {
                rankString.append((board & (1L << location)) == 0 ? '0' : '1');
            }

            ranks.add(rankString.toString());
        }

        int i = 0;
        for (String r : ranks) {
            LOGGER.info("{}: {}", Integer.valueOf(8 - i), r);
            i++;
        }

        LOGGER.info("   abcdefgh");
    }

    public static String longToReversedBinaryString(long l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Long.numberOfLeadingZeros(l); i++) {
            sb.append('0');
        }

        sb.append(Long.toBinaryString(l));

        return sb.reverse().toString();
    }

    public static void printBoardv2(long board) {
        String reversedLong = longToReversedBinaryString(board);

        for (int rank = 7; rank >= 0; rank--) {
            LOGGER.info("{}: {}",
                    Integer.valueOf(rank + 1),
                    reversedLong.substring(rank * 8, (rank + 1) * 8));
        }

        LOGGER.info("   abcdefgh");
    }

    public int[] getPawnLocations(PlayerColor activePlayerColor) {
        Piece p;
        if (activePlayerColor == PlayerColor.WHITE)
            p = Piece.WHITE_PAWN;
        else if (activePlayerColor == PlayerColor.BLACK)
            p = Piece.BLACK_PAWN;
        else
            throw new IllegalArgumentException("Not a valid PlayerColor");

        return Board.bitboardToArray(this.boards[p.getIndex()]);
    }

    public int[] getRookLocations(PlayerColor activePlayerColor) {
        Piece p;
        if (activePlayerColor == PlayerColor.WHITE)
            p = Piece.WHITE_ROOK;
        else if (activePlayerColor == PlayerColor.BLACK)
            p = Piece.BLACK_ROOK;
        else
            throw new IllegalArgumentException("Not a valid PlayerColor");

        return Board.bitboardToArray(this.boards[p.getIndex()]);
    }


	public int[] getKnightLocations(PlayerColor activePlayerColor) {
        Piece p;
        if (activePlayerColor == PlayerColor.WHITE)
            p = Piece.WHITE_KNIGHT;
        else if (activePlayerColor == PlayerColor.BLACK)
            p = Piece.BLACK_KNIGHT;
        else
            throw new IllegalArgumentException("Not a valid PlayerColor");

        return Board.bitboardToArray(this.boards[p.getIndex()]);
	}

    protected void clearLocation(int location) {
        for(int i = 0; i < this.boards.length; i++) {
            // clear the bit at the location on all boards
            this.boards[i] &= ~(1L << location);
        }
    }

    public static int countSetBits(long n) {
        // base case
        if (n == 0)
            return 0;
        else
            return 1 + countSetBits(n & (n - 1L));
    }

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

}
