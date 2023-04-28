package com.stateofflux.chess.model;

import java.util.Scanner;

/**
 * https://www.chessprogramming.org/Forsyth-Edwards_Notation
 * <FEN> ::= <Piece Placement>
 * ' ' <Side to move>
 * ' ' <Castling ability>
 * ' ' <En passant target square>
 * ' ' <Halfmove clock>
 * ' ' <Fullmove counter>
 */
public class FenString {
    private String piecePlacement;
    private PlayerColor playerTurn;
    private char[] castlingRights;
    private char[] enPassantTarget;
    private int halfmoveClock = 0;
    private int fullmoveCounter = 1;

    public FenString(String fen) {
        try (Scanner s = new Scanner(fen)) {
            this.piecePlacement = s.next();
            setActivePlayerColor(s.next());
            setCastlingRights(s.next());
            setEnPassantTarget(s.next());

            if(s.hasNextInt()) {
                setHalfmoveClock(s.next());
                setFullmoveCounter(s.next());
            }
        }
    }

    public static String locationToAlgebraString(int location) {
        int rank = location / 8;
        int file = location % 8;

        return String.format("%c%d", 'a' + file, 8 - rank);
    }

    /*
     * <Piece Placement> ::=
     * <rank8>'/'<rank7>'/'<rank6>'/'<rank5>'/'<rank4>'/'<rank3>'/'<rank2>'/'<rank1>
     * <ranki> ::= [<digit17>]<piece> {[<digit17>]<piece>} [<digit17>] | '8'
     * <piece> ::= <white Piece> | <black Piece>
     * <digit17> ::= '1' | '2' | '3' | '4' | '5' | '6' | '7'
     * <white Piece> ::= 'P' | 'N' | 'B' | 'R' | 'Q' | 'K'
     * <black Piece> ::= 'p' | 'n' | 'b' | 'r' | 'q' | 'k'
     */
    public String getPiecePlacement() {
        return this.piecePlacement;
    }

    /*
     * <Side to move> ::= {'w' | 'b'}
     */
    private void setActivePlayerColor(String playerColorString) {
        char playerColorChar = playerColorString.charAt(0);

        this.playerTurn = switch (playerColorChar) {
            case 'w' -> PlayerColor.WHITE;
            case 'b' -> PlayerColor.BLACK;
            default -> throw new IllegalArgumentException("Invalid player turn: " + playerColorChar);
        };
    }

    public PlayerColor getActivePlayerColor() {
        return this.playerTurn;
    }

    /*
     * <Castling ability> ::= '-' | ['K'] ['Q'] ['k'] ['q'] (1..4)
     */
    private void setCastlingRights(String castlingRightsString) {
        this.castlingRights = castlingRightsString.toCharArray();
    }

    public char[] getCastlingRights() {
        return this.castlingRights;
    }

    /*
     * <En passant target square> ::= '-' | <epsquare>
     * <epsquare> ::= <fileLetter> <eprank>
     * <fileLetter> ::= 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     * <eprank> ::= '3' | '6'
     */
    private void setEnPassantTarget(String enPassantTargetString) {
        // TODO add validation
        this.enPassantTarget = enPassantTargetString.toCharArray();
    }
    public char[] getEnPassantTarget() {
        return this.enPassantTarget;
    }

    /*
     * <Halfmove Clock> ::= <digit> {<digit>}
     * <digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
     */
    private void setHalfmoveClock(String halfmoveClockString) {
        this.halfmoveClock = Integer.parseInt(halfmoveClockString);
    }

    public int getHalfmoveClock() {
        return this.halfmoveClock;
    }

    /*
     * <Fullmove counter> ::= <digit19> {<digit>}
     * <digit19> ::= '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * <digit> ::= '0' | <digit19>
     */
    private void setFullmoveCounter(String fullmoveCounterString) {
        this.fullmoveCounter = Integer.parseInt(fullmoveCounterString);
    }

    public int getFullmoveCounter() {
        return this.fullmoveCounter;
    }

    public static void populateBoard(Board b, String fen) {
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

            location = b.setPieceOnBoard(element, location);
        }
    }
}
