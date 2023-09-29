package com.stateofflux.chess.model;

import java.util.Scanner;

import com.stateofflux.chess.model.pieces.PawnMoves;

/**
 * See https://www.chessprogramming.org/Algebraic_Chess_Notation#Enpassant
 *
 * This class parses FEN Strings and ensures the values
 * are valid. The Game class ensures checks if the moves
 * are valid within the context of the game.
 *
 * https://www.chessprogramming.org/Forsyth-Edwards_Notation
 * <FEN> ::= <Piece Placement>
 * ' ' <Side to move>
 * ' ' <Castling ability>
 * ' ' <En passant target square>
 * ' ' <Halfmove clock>
 * ' ' <Fullmove counter>
 */
public class FenString {
    public static final char WHITE_KING_SIDE_CASTLE = 'K';
    public static final char WHITE_QUEEN_SIDE_CASTLE = 'Q';
    public static final char BLACK_KING_SIDE_CASTLE = 'k';
    public static final char BLACK_QUEEN_SIDE_CASTLE = 'q';
    public static final char NO_CASTLING = '-';

    private String piecePlacement;
    private PlayerColor playerTurn;
    private String castlingRights;
    private String enPassantTarget;
    private int halfmoveClock = 0;
    private int fullmoveCounter = 1;

    public FenString(String fen) {
        try (Scanner s = new Scanner(fen)) {
            this.piecePlacement = s.next();
            setActivePlayerColor(s.next());
            setCastlingRights(s.next());
            setEnPassantTarget(s.next());

            if (s.hasNextInt()) {
                setHalfmoveClock(s.next());
                setFullmoveCounter(s.next());
            }
        }
    }

    public static String locationToSquare(int location) {
        if (location < 0 || location > 63) // can be done with boolean math
            throw new IllegalArgumentException("Invalid location");

        int rank = location / 8;
        int file = location % 8;

        return String.format("%c%d", 'a' + file, rank + 1);
    }

    // https://www.chess.com/terms/chess-notation
    public static int squareToLocation(String target) {
        // if the size is 2, then it is a pawn move
        boolean check = false;
        boolean checkmate = false;
        String square;

        if (target.charAt(target.length() - 1) == '+')
            check = true;
        else if (target.charAt(target.length() - 1) == '*')
            checkmate = true;

        if (check || checkmate)
            square = target.substring(0, target.length() - 1);
        else
            square = target;

        if (square.length() == 2) {
            return simpleSquareToLocation(square);
        } else if (square.length() == 3) {
            return simpleSquareToLocation(square.substring(1, 3));
        } else if (square.length() == 4 && square.charAt(1) == 'x') {
            return simpleSquareToLocation(square.substring(2, 4));
        } else if (square.length() == 5 && square.charAt(2) == 'x') {
            return simpleSquareToLocation(square.substring(3, 5));
        }

        throw new IllegalArgumentException("target " + target +  " cannot be parsed");
    }

    // must be 2 characters
    private static int simpleSquareToLocation(String target) {
        int file = -1;
        int rank = 0;

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
        for (char c : castlingRightsString.toCharArray()) {
            switch (c) {
                case WHITE_KING_SIDE_CASTLE,
                    WHITE_QUEEN_SIDE_CASTLE,
                    BLACK_KING_SIDE_CASTLE,
                    BLACK_QUEEN_SIDE_CASTLE,
                    NO_CASTLING:
                    continue;
                default:
                    throw new IllegalArgumentException("Invalid castling rights: " + castlingRightsString);
            }
        }

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

}
