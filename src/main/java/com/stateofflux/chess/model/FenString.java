package com.stateofflux.chess.model;

import java.util.Scanner;
import java.util.regex.Pattern;

import com.stateofflux.chess.model.pieces.PawnMoves;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * See <a href="https://www.chessprogramming.org/Algebraic_Chess_Notation#Enpassant">En Passant</a>
 * <br/>
 * This class parses <a href="https://www.chessprogramming.org/Forsyth-Edwards_Notation">FEN Strings</a> and ensures the values
 * are valid. The Game class ensures checks if the moves
 * are valid within the context of the game.
 * <br/>
 * <code>
 * <FEN> ::= <Piece Placement>
 * ' ' <Side to move>
 * ' ' <Castling ability>
 * ' ' <En passant target square>
 * ' ' <Halfmove clock>
 * ' ' <Fullmove counter>
 * </code>
 */
public class FenString {
    public static final String INITIAL_BOARD = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";

    public static final char NO_CASTLING = '-';

    private String piecePlacement;
    private PlayerColor playerTurn;
    private String castlingRights;
    private String enPassantTarget;
    private int halfmoveClock = 0;
    private int fullmoveCounter = 1;

    public FenString(String fen) {
        try (Scanner s = new Scanner(fen)) {
            setPiecePlacement(s.next());
            setActivePlayerColor(s.next());
            setCastlingRights(s.next());
            setEnPassantTarget(s.next());

            if (s.hasNextInt()) {
                int halfmove = Integer.parseInt(s.next());
                int fullmove = Integer.parseInt(s.next());

                if(halfmove < 0 || fullmove < 0)
                    throw new NumberFormatException("moves must be greater than 0");

                setHalfmoveClock(halfmove);
                setFullmoveCounter(fullmove);
            }
        }
    }

    private void setPiecePlacement(String pp) {
        String[] parts = pp.split("/");

        if(parts.length != 8)
            throw new IllegalArgumentException("board definition requires 8 sections");

        for(int i = 0; i < parts.length; i++) {
            int pieces = 0;
            for(int j = 0; j < parts[i].length(); j++) {
                char c = parts[i].charAt(j);
                if(Character.isDigit(c))
                    pieces += Character.digit(c, 10);
                else
                    pieces++;
            }

            if(pieces != 8)
                throw new IllegalArgumentException("board sections must represent 8 squares");
        }

        this.piecePlacement = pp;
    }

    public static String locationToSquare(int location) {
        if (location < 0 || location > 63) // can be done with boolean math
            throw new IllegalArgumentException("Invalid location");

        int rank = Board.rank(location);
        int file = Board.file(location);

        return String.format("%c%d", 'a' + file, rank + 1);
    }

    // https://www.chess.com/terms/chess-notation
    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "Switch statement is looking for an optional last character")
    public static int squareToLocation(String target) {
        // if the size is 2, then it is a pawn move
        boolean check = false;
        boolean checkmate = false;
        String square;

        switch(target.charAt(target.length() - 1)) {
            case '+':
                check = true;
                break;
            case '*':
                break;
            case '#':
                checkmate = true;
                break;
        }

        if (check || checkmate)
            square = target.substring(0, target.length() - 1);
        else
            square = target;

        int length = square.length();
        return simpleSquareToLocation(square.substring(length - 2, length));
    }

    // must be 2 characters
    private static int simpleSquareToLocation(String target) {
        int file;
        int rank;

        char fileChar;
        char rankChar;

        fileChar = target.charAt(0);
        rankChar = target.charAt(1);

        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8')
            throw new IllegalArgumentException("Invalid target");

        file = fileChar - 'a';
        rank = (rankChar - '1');

        return rank * 8 + file;
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
        if(!Pattern.matches("^(?!(.*K){2})(?!(.*Q){2})(?!(.*k){2})(?!(.*q){2})[KQkq]{0,4}$|-", castlingRightsString))
            throw new IllegalArgumentException("Invalid castling rights: " + castlingRightsString);

        this.castlingRights = castlingRightsString;
    }

    public String getCastlingRights() {
        return this.castlingRights;
    }

    /*
     * <En passant target square> ::= '-' | <epsquare>
     * <epsquare> ::= <fileLetter> <eprank>
     * <fileLetter> ::= 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h'
     * <eprank> ::= '3' | '6'
     */
    private void setEnPassantTarget(String enPassantTargetString) {
        if (enPassantTargetString.equals(PawnMoves.NO_EN_PASSANT)) {
            this.enPassantTarget = enPassantTargetString;
            return;
        }

        char fileChar;
        char rankChar;

        if (enPassantTargetString.length() == 2) {
            fileChar = enPassantTargetString.charAt(0);
            rankChar = enPassantTargetString.charAt(1);

            if (!((fileChar >= 'a' && fileChar <= 'h') &&
                    (rankChar == '3' || rankChar == '6'))) {
                throw new IllegalArgumentException("Invalid target");
            }
        }

        this.enPassantTarget = enPassantTargetString;
    }

    public String getEnPassantTarget() {
        return this.enPassantTarget;
    }

    /*
     * <Halfmove Clock> ::= <digit> {<digit>}
     * <digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
     */
    private void setHalfmoveClock(int halfmoveClockString) {
        this.halfmoveClock = halfmoveClockString;
    }

    public int getHalfmoveClock() {
        return this.halfmoveClock;
    }

    /*
     * <Fullmove counter> ::= <digit19> {<digit>}
     * <digit19> ::= '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
     * <digit> ::= '0' | <digit19>
     */
    private void setFullmoveCounter(int fullmoveCounterString) {
        this.fullmoveCounter = fullmoveCounterString;
    }

    public int getFullmoveCounter() {
        return this.fullmoveCounter;
    }

}
