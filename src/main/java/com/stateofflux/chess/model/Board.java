package com.stateofflux.chess.model;

import java.util.BitSet;

public class Board {
    protected BitSet whiteKing;
    protected BitSet whiteQueens;
    protected BitSet whiteRooks;
    protected BitSet whiteBishops;
    protected BitSet whiteKnights;
    protected BitSet whitePawns;
    protected BitSet blackKing;
    protected BitSet blackQueens;
    protected BitSet blackRooks;
    protected BitSet blackBishops;
    protected BitSet blackKnights;
    protected BitSet blackPawns;
    protected BitSet[] boards;

/**
    RANKS:
    8 |       56   57   58   59   60   61   62   63  (MSB,
    7 |       48   49   50   51   52   53   54   55  left)
    6 |       40   41   42   43   44   45   46   47
    5 |       32   33   34   35   36   37   38   39
    4 |       24   25   26   27   28   29   30   31
    3 |       16   17   18   19   20   21   22   23
    2 |        8    9   10   11   12   13   14   15
    1 | (LSB,  0    1    2    3    4    5    6    7
        right)
            -------------------------------------------
  FILES:      a     b    c    d    e    f    g    h



8	♜  ♞  ♝  ♛  ♚  ♝  ♞  ♜
7	♟︎  ♟︎  ♟︎  ♟︎  ♟︎  ♟︎  ♟︎  ♟︎
6
5
4
3
2	♙  ♙  ♙  ♙  ♙  ♙  ♙  ♙
1	♖  ♘  ♗  ♕  ♔  ♗  ♘  ♖
    a  b  c  d  e  f  g  h

  */

    public Board() {
        this.whiteKing = new BitSet(64);
        this.whiteQueens = new BitSet(64);
        this.whiteRooks = new BitSet(64);
        this.whiteBishops = new BitSet(64);
        this.whiteKnights = new BitSet(64);
        this.whitePawns = new BitSet(64);
        this.blackKing = new BitSet(64);
        this.blackQueens = new BitSet(64);
        this.blackRooks = new BitSet(64);
        this.blackBishops = new BitSet(64);
        this.blackKnights = new BitSet(64);
        this.blackPawns = new BitSet(64);
        this.boards = new BitSet[] {
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

        this.whiteKing.set(4);
        this.whiteQueens.set(3);
        this.whiteRooks.set(0);  this.whiteRooks.set(7);
        this.whiteBishops.set(2); this.whiteBishops.set(5);
        this.whiteKnights.set(1); this.whiteKnights.set(6);
        this.whitePawns.set(8, 16);

        this.blackKing.set(60);
        this.blackQueens.set(59);
        this.blackRooks.set(56); this.blackRooks.set(63);
        this.blackBishops.set(58); this.blackBishops.set(61);
        this.blackKnights.set(57); this.blackKnights.set(62);
        this.blackPawns.set(48, 56);
    }

    protected String getPieceAtLocation(int location) {
        for(int boardCount = 0; boardCount < this.boards.length; boardCount++) {
            if(this.boards[boardCount].get(location)) {
                switch(boardCount) {
                    case 0: return "WK";
                    case 1: return "WQ";
                    case 2: return "WR";
                    case 3: return "WB";
                    case 4: return "WN";
                    case 5: return "WP";
                    case 6: return "BK";
                    case 7: return "BQ";
                    case 8: return "BR";
                    case 9: return "BB";
                    case 10: return "BN";
                    case 11: return "BP";
                }
            }
        }
        return "  ";
    }

    public void printBoard() {
        int pieceChars = 2;
        StringBuilder prettyBoard = new StringBuilder(64 * pieceChars);

        for (int i = 0; i < 64; i++) {
            prettyBoard.insert(i * pieceChars, getPieceAtLocation(i));
        }

        CharSequence[] boardChars = new CharSequence[8];

        for (int i = 7; i >= 0; i--) {
            boardChars[i] = prettyBoard.subSequence(i * 8 * pieceChars, (i + 1) * pieceChars * 8);
            System.out.printf("%d: %s\n", i + 1, boardChars[i]);
        }

        System.out.println("   a b c d e f g h");
    }
}
