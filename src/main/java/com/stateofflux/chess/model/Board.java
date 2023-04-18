package com.stateofflux.chess.model;

import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class uses Forsyth–Edwards Notation
 *
 * https://en.wikipedia.org/wiki/Forsyth–Edwards_Notation
 */
public class Board {
    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    protected static final int WHITE_KING_INDEX = 0;
    protected static final int WHITE_QUEENS_INDEX = 1;
    protected static final int WHITE_ROOKS_INDEX = 2;
    protected static final int WHITE_BISHOPS_INDEX = 3;
    protected static final int WHITE_KNIGHTS_INDEX = 4;
    protected static final int WHITE_PAWNS_INDEX = 5;
    protected static final int BLACK_KING_INDEX = 6;
    protected static final int BLACK_QUEENS_INDEX = 7;
    protected static final int BLACK_ROOKS_INDEX = 8;
    protected static final int BLACK_BISHOPS_INDEX = 9;
    protected static final int BLACK_KNIGHTS_INDEX = 10;
    protected static final int BLACK_PAWNS_INDEX = 11;

    protected static final char WHITE_KING_CHAR = 'K';
    protected static final char WHITE_QUEENS_CHAR = 'Q';
    protected static final char WHITE_ROOKS_CHAR = 'R';
    protected static final char WHITE_BISHOPS_CHAR = 'B';
    protected static final char WHITE_KNIGHTS_CHAR = 'N';
    protected static final char WHITE_PAWNS_CHAR = 'P';
    protected static final char BLACK_KING_CHAR = 'k';
    protected static final char BLACK_QUEENS_CHAR = 'q';
    protected static final char BLACK_ROOKS_CHAR = 'r';
    protected static final char BLACK_BISHOPS_CHAR = 'b';
    protected static final char BLACK_KNIGHTS_CHAR = 'n';
    protected static final char BLACK_PAWNS_CHAR = 'p';

    protected static final char[] boardChars = new char[] {
            WHITE_KING_CHAR,
            WHITE_QUEENS_CHAR,
            WHITE_ROOKS_CHAR,
            WHITE_BISHOPS_CHAR,
            WHITE_KNIGHTS_CHAR,
            WHITE_PAWNS_CHAR,
            BLACK_KING_CHAR,
            BLACK_QUEENS_CHAR,
            BLACK_ROOKS_CHAR,
            BLACK_BISHOPS_CHAR,
            BLACK_KNIGHTS_CHAR,
            BLACK_PAWNS_CHAR
    };

    protected long whiteKing;
    protected long whiteQueens;
    protected long whiteRooks;
    protected long whiteBishops;
    protected long whiteKnights;
    protected long whitePawns;
    protected long blackKing;
    protected long blackQueens;
    protected long blackRooks;
    protected long blackBishops;
    protected long blackKnights;
    protected long blackPawns;
    protected long[] boards;

    /**
     * RANKS:
     * 8 | 56 57 58 59 60 61 62 63 (MSB,
     * 7 | 48 49 50 51 52 53 54 55 left)
     * 6 | 40 41 42 43 44 45 46 47
     * 5 | 32 33 34 35 36 37 38 39
     * 4 | 24 25 26 27 28 29 30 31
     * 3 | 16 17 18 19 20 21 22 23
     * 2 | 8 9 10 11 12 13 14 15
     * 1 | (LSB, 0 1 2 3 4 5 6 7
     * right)
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
        this.whiteKing = 1L << 4;
        this.whiteQueens = 1L << 3;
        this.whiteRooks = 1L | 1L << 7;
        this.whiteBishops = 1L << 2 | 1L << 5;
        this.whiteKnights = 1L << 1 | 1L << 6;
        this.whitePawns = 255L << 8;

        this.blackKing = 1L << 60;
        this.blackQueens = 1L << 59;
        this.blackRooks = 1L << 63 | 1L << 56;
        this.blackBishops = 1L << 58 | 1L << 61;
        this.blackKnights = 1L << 57 | 1L << 62;
        this.blackPawns = 255L << 48;

        updateBoardsArray();  // initialize the boards with values
    }

    /*
     * Build a board using a fen string
     */
    public Board(String fen) {
        updateBoardsArray();  // start with empty boards

        char[] fenCh = fen.toCharArray();
        int boardPosition = 0;

        for (char element : fenCh) {
            if (element == '/') {
                continue;
            }

            // break if the fenCH[i] is a digit
            if (Character.isDigit(element)) {
                boardPosition += Character.digit(element, 10);
                continue;
            }

            for (int j = 0; j < this.boardChars.length; j++) {
                if (element == this.boardChars[j]) {
                    this.boards[j] |= 1L << boardPosition;
                    boardPosition++;
                    break;
                }
            }
        }
    }

    private void updateBoardsArray() {
        // BitSet is inefficient according to
        // https://github.com/brettwooldridge/SparseBitSet which
        // proposes a more efficient and faster solution (according to the author).
        // or just use a "long" as it has 64 bits.
        this.boards = new long[] {
                this.whiteKing,
                this.whiteQueens,
                this.whiteRooks,
                this.whiteBishops,
                this.whiteKnights,
                this.whitePawns,
                this.blackKing,
                this.blackQueens,
                this.blackRooks,
                this.blackBishops,
                this.blackKnights,
                this.blackPawns
        };
    };

    /*
     * return the characture representing a piece
     */
    protected char getPieceAtLocation(int location) {
        for (int boardCount = 0; boardCount < this.boards.length; boardCount++) {
            // is the location occupied for this board?
            if ((this.boards[boardCount] & (1L << location)) != 0) // can return both +ve and -ve as longs are signed
                return this.boardChars[boardCount];
        }
        return ' ';
    }

    // public BitSet getOccupied() {
    // BitSet occupied = new BitSet(64);

    // for(int i = 0; i < this.boards.length; i++) {
    // occupied.or(this.boards[i]);
    // }

    // return occupied;
    // }

    protected int getBitSetIndexAtLocation(int location) {
        for (int boardCount = 0; boardCount < this.boards.length; boardCount++) {
            if ((this.boards[boardCount] & (1L << location)) > 0)
                return boardCount;
        }
        return -1;
    }

    // starting from location, but not looking ahead more than max, find the next
    // piece on the board
    // TODO: Could this be done as a BitSet operation?
    protected int nextPiece(int location, int max) {
        int i = 1;

        while (location + i < 64 &&
                i < max &&
                this.getPieceAtLocation(location + i) == ' ')
            i++;

        return i + location;
    }

    // implement a Forsyth-Edwards toString() method
    public String toFenString() {
        StringBuilder f = new StringBuilder(); // TODO - initialize with size.

        int i = 0;
        int n = 0;
        int max = 0;
        char currentPiece;

        while (i < 64) {
            currentPiece = getPieceAtLocation(i);

            // if the next space is empty, look to the end of the line to see how many
            // emptpy chars exists and concat the number of empty spaces as an int.
            // otherwise, add the next piece to the string.
            if (currentPiece == ' ') {
                max = 8 - (i % 8);
                n = this.nextPiece(i, max);
                f.append(n - i); // use a number to show how many spaces are left
            } else {
                f.append(currentPiece);
                n = i + 1;
            }

            i = n;

            if (i % 8 == 0 && i != 64)
                f.append('/');
        }

        return f.toString();
    }

    /*
     * Move a piece on the board, but do not perform validation.
     */
    public boolean move(String from, String to) {
        int fromIndex = Board.convertStringToIndex(from);
        int toIndex = Board.convertStringToIndex(to);

        // attempting to move from an empty location
        if (fromIndex == -1)
            return false; // I should throw an exception as this should never happen.

        int boardIndex = this.getBitSetIndexAtLocation(fromIndex);
        this.boards[boardIndex] ^= (1L << fromIndex); // clear
        this.boards[boardIndex] |= (1L << toIndex); // set

        return true;
    }

    public static int convertStringToIndex(String position) {
        int rank = Integer.parseInt(position.substring(1, 2));
        int file = position.charAt(0) - 'a';

        return (rank - 1) * 8 + file;
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

        LOGGER.info("   a b c d e f g h");
    }
}
