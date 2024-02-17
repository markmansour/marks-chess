package com.stateofflux.chess;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.player.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class App
{
    private static final Logger Logger = LoggerFactory.getLogger(Game.class);
    private static final String PROGRAM_NAME = "Mark's Chess Program";

    /*
     * To run this from the command line:
     * $ cd /Users/markmansour/IdeaProjects/marks-chess
     * $ mvn package assembly:single
     */
    public static void main(String[] args)
    {
        Logger.info(PROGRAM_NAME);

        AppArgs aa = new AppArgs();
        JCommander.Builder builder = JCommander.newBuilder().addObject(aa);
        JCommander jc = builder.build();
        jc.setProgramName(PROGRAM_NAME);

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.out.println("Error parsing command line args: " + String.join(" ", args) );
            e.usage();
            System.exit(1);
        }

        if(aa.askedForHelp) {
            jc.usage();
            System.exit(0);
        }

        try {
            // Evaluator evaluator = new SimpleEvaluator();
            Class<?> evaluatorClass = Class.forName("com.stateofflux.chess.model.player." + aa.evaluatorStrategy);
            Evaluator evaluator = (Evaluator) evaluatorClass.getConstructor().newInstance();

            // Player whitePlayer = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
            Class<?> whitePlayerClass = Class.forName("com.stateofflux.chess.model.player." + aa.whiteStrategy);
            Constructor<?> whitePlayerConstructor = whitePlayerClass.getConstructor(PlayerColor.class, Evaluator.class);
            Player whitePlayer = (Player) whitePlayerConstructor.newInstance(PlayerColor.WHITE, evaluator);

            // Player blackPlayer = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
            Class<?> blackPlayerClass = Class.forName("com.stateofflux.chess.model.player." + aa.blackStrategy);
            Constructor<?> blackPlayerConstructor = blackPlayerClass.getConstructor(PlayerColor.class, Evaluator.class);
            Player blackPlayer = (Player) blackPlayerConstructor.newInstance(PlayerColor.WHITE, evaluator);

            // give the players a reasonable chance of winning.
            whitePlayer.setSearchDepth(aa.whiteDepth);
            blackPlayer.setSearchDepth(aa.blackDepth);

            uciLoop(whitePlayer, blackPlayer, aa);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void uciLoop(Player whitePlayer, Player blackPlayer, AppArgs aa) {
        StringBuilder sb = new StringBuilder();

        final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        Game game = null;

        Logger.atInfo().log("Configuration: ");
        Logger.atInfo().log("- white player type: " + aa.whiteStrategy);
        Logger.atInfo().log("- white player search depth: " + aa.whiteDepth);
        Logger.atInfo().log("- black player type: " + aa.blackStrategy);
        Logger.atInfo().log("- black player search depth: " + aa.blackDepth);
        Logger.atInfo().log("- evaluator: " + aa.evaluatorStrategy);
        Logger.atInfo().log("- help?: " + aa.askedForHelp);
        Logger.atInfo().log("");

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
                case "isready" -> System.out.println("readyok");
                case "setoption" -> throw new IllegalArgumentException("setoption not supported yet");
                case "register" -> System.out.println("later");
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
                default -> Logger.info("Error: {}", line);
            }

            sb.append(line).append(System.lineSeparator());
        }

        Logger.info(sb.toString());
    }
}
