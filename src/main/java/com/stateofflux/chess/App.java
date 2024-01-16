package com.stateofflux.chess;

import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.BasicNegaMaxPlayer;
import com.stateofflux.chess.model.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger Logger = LoggerFactory.getLogger(Game.class);

    /*
     * To run this from the command line:
     * $ cd /Users/markmansour/IdeaProjects/marks-chess
     * $ mvn compile exec:java -Dexec.mainClass=com.stateofflux.chess.App
     */
    public static void main( String[] args )
    {
        Logger.info("Mark's Chess Program");
        StringBuilder sb = new StringBuilder();
        final Scanner scanner = new Scanner(System.in);
        Game game = null;
        Player whitePlayer = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player blackPlayer = new BasicNegaMaxPlayer(PlayerColor.BLACK);

        for (String line = scanner.nextLine().strip(); !line.equals("quit"); line = scanner.nextLine()) {
            String[] lineParts = line.split("\\s+");
            Logger.info("command received: {}", line);

            switch(lineParts[0]) {
                case "uci" -> {
                    System.out.println("id name Mark's Chess Engine");
                    System.out.println("id author Mark Mansour");
//                     System.out.println("option name <OPTION-NAME> value");
                    System.out.println("uciok");
                }
                case "isready" -> {
                    System.out.println("readyok");
                }
                case "setoption" -> {
                    throw new IllegalArgumentException("setoption not supported yet");
                }
                case "register" -> {
                    System.out.println("later");
                }
                case "go" -> {
                    assert game != null;
                    Logger.info("Ignoring all go commands");
                    Player p = game.getActivePlayerColor().isWhite() ? whitePlayer : blackPlayer;
                    Move m = p.getNextMove(game);
                    System.out.println("bestmove " + m.toLongSan());
                }
                case "ucinewgame" -> {
                    game = new Game();
                    game.setPlayers(whitePlayer, blackPlayer);
                }
                case "position" -> {
                    String fen = FenString.INITIAL_BOARD;

                    if(lineParts[1].equals("startpos"))
                        Logger.info("using default board setup");
/*
                    else
                        fen = lineParts[2];
*/

                    game = new Game(fen);
                    game.setPlayers(whitePlayer, blackPlayer);

                    if (lineParts.length >= 4 && lineParts[2].equals("moves")) {
                        int movesOffset = 3;
                        int i = movesOffset;
                        while(i < lineParts.length) {
                            assert game != null;
                            game.moveLongNotation(lineParts[i]);
                            i++;
                        }
                    }
                }
                case "d" -> {
                    assert game != null;
                    game.printOccupied();
                }
                default -> {
                    Logger.info("Error: {}", line);
                }
            }

            sb.append(line).append(System.lineSeparator());
        }

        Logger.info(sb.toString());
    }
}
