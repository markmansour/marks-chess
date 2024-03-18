package com.stateofflux.chess;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.stateofflux.chess.model.*;
import com.stateofflux.chess.model.player.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class App
{
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public final static Logger uci_logger = LoggerFactory.getLogger("UCI_Logger");

    private static final String PROGRAM_NAME = "Mark's Chess Program";
    private final Player whitePlayer;
    private final Player blackPlayer;
    private int hashSize = TranspositionTable.DEFAULT_HASH_SIZE_IN_MB;

    /*
     * To run this from the command line:
     * $ cd /Users/markmansour/IdeaProjects/marks-chess
     * $ mvn package assembly:single
     */
    public static void main(String[] args)
    {
        logger.info(PROGRAM_NAME);

        AppArgs aa = new AppArgs();
        JCommander.Builder builder = JCommander.newBuilder().addObject(aa);
        JCommander jc = builder.build();
        jc.setProgramName(PROGRAM_NAME);

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            logger.atError().log("Error parsing command line args: " + String.join(" ", args) );
            e.usage();
            System.exit(1);
        }

        if(aa.askedForHelp) {
            jc.usage();
            System.exit(0);
        }

        logger.atDebug().log("Configuration: ");
        logger.atDebug().log("- white player type: " + aa.whiteStrategy);
        logger.atDebug().log("- white player search depth: " + aa.whiteDepth);
        logger.atDebug().log("- black player type: " + aa.blackStrategy);
        logger.atDebug().log("- black player search depth: " + aa.blackDepth);
        logger.atDebug().log("- evaluator: " + aa.evaluatorStrategy);
        logger.atDebug().log("- help?: " + aa.askedForHelp);
        logger.atDebug().log("");

        try {
            App app = new App(aa);
            app.uciLoop();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public App(AppArgs aa) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Evaluator evaluator = new SimpleEvaluator();
        Class<?> evaluatorClass = Class.forName("com.stateofflux.chess.model.player." + aa.evaluatorStrategy);
        Evaluator evaluator = (Evaluator) evaluatorClass.getConstructor().newInstance();

        // Player whitePlayer = new AlphaBetaPlayer(PlayerColor.WHITE, evaluator);
        Class<?> whitePlayerClass = Class.forName("com.stateofflux.chess.model.player." + aa.whiteStrategy);
        Constructor<?> whitePlayerConstructor = whitePlayerClass.getConstructor(PlayerColor.class, Evaluator.class);
        whitePlayer = (Player) whitePlayerConstructor.newInstance(PlayerColor.WHITE, evaluator);

        // Player blackPlayer = new AlphaBetaPlayer(PlayerColor.BLACK, evaluator);
        Class<?> blackPlayerClass = Class.forName("com.stateofflux.chess.model.player." + aa.blackStrategy);
        Constructor<?> blackPlayerConstructor = blackPlayerClass.getConstructor(PlayerColor.class, Evaluator.class);
        blackPlayer = (Player) blackPlayerConstructor.newInstance(PlayerColor.WHITE, evaluator);

        // give the players a reasonable chance of winning.
        whitePlayer.setSearchDepth(aa.whiteDepth);
        blackPlayer.setSearchDepth(aa.blackDepth);
    }

    public void uciLoop() {
        logger.atInfo().log("log level info? " + logger.isInfoEnabled());
        logger.atDebug().log("log level debug? " + logger.isDebugEnabled());
        StringBuilder sb = new StringBuilder();

        final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        Game game = null;

        for (String line = scanner.nextLine().strip(); !line.equals("quit"); line = scanner.nextLine()) {
            String[] lineParts = line.split("\\s+");
            logger.atDebug().log("command received: {}", line);

            switch(lineParts[0]) {
                case "uci" -> {
                    uci_logger.atInfo().log("id name Mark's Chess Engine");
                    uci_logger.atInfo().log("id author Mark Mansour");

                    // e.g. option name Hash type spin default 64 min 1 max 65536
                    uci_logger.atInfo().log("option name Hash type spin default " + TranspositionTable.DEFAULT_HASH_SIZE_IN_MB + " min 1 max " + getFreeMemoryInMB());
//                     System.out.println("option name <OPTION-NAME> value");
                    uci_logger.atInfo().log("uciok");
                }
                case "isready" -> uci_logger.atInfo().log("readyok");
                case "setoption" -> {
                    // e.g. setoption name Hash value 64
                    if(lineParts[2].equals("Hash")) {
                        hashSize = Integer.parseInt(lineParts[4]);
                        logger.atDebug().log("set hash size to {}", hashSize);
                    }
                }
                case "register" -> uci_logger.atInfo().log("later");
                case "go" -> {
                    if(game == null) {
                        game = new Game();
                    }

                    if(lineParts.length == 3 && lineParts[1].equals("perft")) {
                        perft(lineParts, game);
                    } else {
                        logger.atInfo().log("parsing the go command");
                        // go wtime 19653 btime 20000 movestogo 40
                        Map<String, String> params = new HashMap<>();
                        for(int i = 1; i < lineParts.length; i += 2)
                            params.put(lineParts[i], lineParts[i+1]);

                        Player p = game.getActivePlayerColor().isWhite() ? whitePlayer : blackPlayer;

                        // wtime & btime
                        String timeString = game.getActivePlayerColor().isWhite() ? params.get("wtime") : params.get("btime");
                        logger.atDebug().log("timeString : " + timeString);

                        if(!timeString.isBlank()) {
                            long remainingTimeInMillis = Integer.parseInt(timeString);
                            int movesToGo = 50;  // assume there are 50 moves left
                            if(params.containsKey("movestogo")) {
                                movesToGo = Integer.parseInt(params.get("movestogo"));
                            }
                            long incrementInNanos = TimeUnit.MILLISECONDS.toNanos(remainingTimeInMillis / movesToGo);
                            incrementInNanos *= 0.9;  // leave 10% of the time for overruns.
                            p.setIncrement(incrementInNanos);
                            logger.atDebug().log("set increment for {} to {}ms", game.getActivePlayerColor(), TimeUnit.NANOSECONDS.toMillis(incrementInNanos));
                        }

                        // movestogo - noop

                        Move m = p.getNextMove(game);
                        uci_logger.atInfo().log("bestmove " + m.toLongSan());
                    }
                }
                case "ucinewgame" -> {
                    game = resetGame();
                }
                case "position" -> {
                    String fen = FenString.INITIAL_BOARD;

                    if(lineParts[1].equals("startpos"))
                        logger.atDebug().log("using default board setup");

                    game = resetGame(fen);

                    if (lineParts.length >= 4 && lineParts[2].equals("moves")) {
                        int i = 3;
                        while(i < lineParts.length) {
                            game.moveLongNotation(lineParts[i]);
                            i++;
                        }

                        logger.atDebug().log("after position startpos: fen \"{}\"", game.asFen());
                    }
                }
                case "d" -> {
                    assert game != null;
                    game.printOccupied();
                }
                case "help" -> {
                    uci_logger.atInfo().log("Mark's Chess is a chess engine for playing and analyzing.");
                    uci_logger.atInfo().log("Mark's Chess is normally used with a graphical user interface (GUI) and implements");
                    uci_logger.atInfo().log("the Universal Chess Interface (UCI) protocol to communicate with a GUI, an API, etc.");
                    uci_logger.atInfo().log("For any further information, visit https://github.com/markmansour/marks-chess");
                    uci_logger.atInfo().log("or read the corresponding README.md and Copying.txt files distributed along with this program.");
                }
                default -> uci_logger.atError().log("Unknown command: '{}'. Type help for more information.", line);
            }

            sb.append(line).append(System.lineSeparator());
        }

        // logger.atDebug().log(sb.toString());
    }

    private static void perft(String[] lineParts, Game game) {
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

        logger.atDebug().log("ran for {} ms and reviewed {} nodes.  {} nodes/second",
            TimeUnit.NANOSECONDS.toMillis(endTime - startTime),
            perftCount, nodesPerSecond
        );
    }

    private Game resetGame() {
        return resetGame(FenString.INITIAL_BOARD);
    }

    private Game resetGame(String fen) {
        Game game = new Game(fen);
        logger.atDebug().log("fresh game object");
        whitePlayer.reset();
        whitePlayer.setHashInMb(hashSize);
        blackPlayer.reset();
        blackPlayer.setHashInMb(hashSize);
        return game;
    }

    @SuppressFBWarnings("DM_GC")
    private int getFreeMemoryInMB() {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        return Math.toIntExact(rt.freeMemory() >> 20L);   // divide 1024 (10) * 1024 (10)
    }
}
