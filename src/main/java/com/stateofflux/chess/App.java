package com.stateofflux.chess;

import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.BasicNegaMaxPlayer;
import com.stateofflux.chess.model.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

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
     * $ mvn package assembly:single
     */
    public static void main(String[] args)
    {
        Logger.info("Mark's Chess Program");
        StringBuilder sb = new StringBuilder();

        final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        Game game = null;
        Player whitePlayer = new BasicNegaMaxPlayer(PlayerColor.WHITE);
        Player blackPlayer = new BasicNegaMaxPlayer(PlayerColor.BLACK);

        // give the players a reasonable chance of winning.
        whitePlayer.setSearchDepth(5);
        blackPlayer.setSearchDepth(5);

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
                    if(game == null) {
                        game = new Game();
                    }

                    if(lineParts.length == 3 && lineParts[1].equals("perft")) {
                        int depth = Integer.parseInt(lineParts[2]);
                        long startTime = System.nanoTime();
                        SortedMap<String, Long> actual = game.perftAtRoot(depth);
                        long endTime = System.nanoTime();
                        game.printPerft(actual);
                        long perftCount = actual.values()
                            .stream()
                            .reduce(0L, Long::sum);

                        long nodesPerSecond;
                        if(TimeUnit.NANOSECONDS.toMillis(endTime - startTime) > 0)
                            nodesPerSecond = (perftCount * 1000L) / TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        else
                            nodesPerSecond = 0;

                        Logger.info("ran for {} ms and reviewed {} nodes.  {} nodes/second",
                            TimeUnit.NANOSECONDS.toMillis(endTime - startTime),
                            perftCount, nodesPerSecond
                        );

                    } else {
                        Logger.info("Ignoring all go commands");
                        Player p = game.getActivePlayerColor().isWhite() ? whitePlayer : blackPlayer;
                        Move m = p.getNextMove(game);
                        System.out.println("bestmove " + m.toLongSan());
                    }
                }
                case "ucinewgame" -> {
                    game = new Game();
                    game.setPlayers(whitePlayer, blackPlayer);
                }
                case "position" -> {
                    String fen = FenString.INITIAL_BOARD;

                    if(lineParts[1].equals("startpos"))
                        Logger.info("using default board setup");

                    game = new Game(fen);
                    Logger.info("fresh game object");
                    game.setPlayers(whitePlayer, blackPlayer);

                    if (lineParts.length >= 4 && lineParts[2].equals("moves")) {
                        int i = 3;
                        while(i < lineParts.length) {
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
