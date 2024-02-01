package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class HumanPlayer extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(HumanPlayer.class);
    private final Scanner scanner;

    public HumanPlayer(PlayerColor pc, Evaluator evaluator) {
        super(pc, evaluator);
        scanner = new Scanner(System.in);  // in IntelliJ - Help / Edit Custom VM options.  Add line: -Deditable.java.test.console=true
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        for(int i = 0; i < moves.size(); i++) {
            LOGGER.info("{}. {} {}", i , moves.get(i).toLongSan(), moves.get(i).getPiece().getAlgebraicChar());
        }

        LOGGER.info("Which move number to play?");
        int firstNum;

        try {
            firstNum = Integer.parseInt(scanner.nextLine());

            if(firstNum > moves.size() || firstNum < 1)
                firstNum = 1;
        } catch(NumberFormatException nfe) {
            firstNum = 1;
        }

        return moves.get(firstNum - 1);
    }
}
