package com.stateofflux.chess.perft;

import com.stateofflux.chess.model.FenString;
import com.stateofflux.chess.model.Game;

import one.profiler.AsyncProfiler;
import one.profiler.Events;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/*
 * See JMH repo for examples on how to use the profiler:
 * https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples
 *
 * To execute on the command line: mvn test -D groups='PerformanceTest'
 */
@Tag("PerformanceTest")
public class DepthTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepthTest.class);
    record PerftRecord(String FenString, int d1, int d2, int d3, int d4, int d5, int d6) {}

    private static ArrayList<PerftRecord> perftRecords;
    private static AsyncProfiler asyncProfiler;

    @BeforeAll
    public static void setUp() {
        // from http://www.rocechess.ch/perft.html
        // see Perft-testsuite - http://www.rocechess.ch/perftsuite.zip
        // 126 test cases
        String resourceName = "perftsuite.epd";

        perftRecords = new ArrayList<>();
        asyncProfiler = AsyncProfiler.getInstance();

        try(InputStream contents = DepthTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            assert contents != null;
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(contents, UTF_8))) {
                String text = reader.readLine();
                while (text != null) {
                    Pattern p = Pattern.compile("(.+);D1 (\\d+) ;D2 (\\d+) ;D3 (\\d+) ;D4 (\\d+) ;D5 (\\d+) ;D6 (\\d+)");
                    Matcher m = p.matcher(text);
                    if (m.matches()) {
                        perftRecords.add(
                            new PerftRecord(
                                m.group(1).strip(),
                                Integer.parseInt(m.group(2)),
                                Integer.parseInt(m.group(3)),
                                Integer.parseInt(m.group(4)),
                                Integer.parseInt(m.group(5)),
                                Integer.parseInt(m.group(6)),
                                Integer.parseInt(m.group(7))
                            )
                        );
                    }

                    text = reader.readLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private ArrayList<PerftRecord> perftRecordsSizeOne() {
        return new ArrayList<>(perftRecords.subList(0, 1));
    }

    private ArrayList<PerftRecord> defaultBoard() {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            FenString.INITIAL_BOARD,
            20,
            400,8902,197281,4865609,119060324
        ));

        return list;
    }

    // 3 runs from IntelliJ - 793ms, 770ms, 921ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 208ms, 207ms, 20ms
    @Test public void firstRecordDepthOne() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(1, perftRecordsSizeOne());

    }

    // 3 runs from IntelliJ - 627ms, 592ms, 529ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 271ms, 207ms, 203ms
    // with undo: 56 ms (2 invalid moves)
    @Test public void firstRecordDepthTwo() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(2, perftRecordsSizeOne());
    }

    // 3 runs from IntelliJ - 15s 527ms, 14s 179ms, 13s 594ms.
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 6 sec 311 ms, 5 sec 464 ms, 4 sec 809ms
    // with undo: 336 ms (50 invalid moves)
    @Test public void firstRecordDepthThree() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(3, perftRecordsSizeOne());

    }

    // using undo: 2 seconds
    @Test public void firstRecordDepthFour() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(4, perftRecordsSizeOne());

    }


    // TAKES A LONG TIME TO RUN
    // 33 seconds => 4867157 nodes ~= 146 nodes per ms
    // 2922 ms and reviewed 4865609 nodes.  1665163 nodes/second
    @Test public void firstRecordDepthFive() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(5, perftRecordsSizeOne());
    }

    // 1.4 seconds
    @Test public void allEpdExamplesToDepthOfOne() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(1, perftRecords);
    }

    // 11 seconds
    // 600 ms
    @Test public void allEpdExamplesToDepthOfTwo() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(2, perftRecords);
    }

    // 3 mins 41 seconds
    // 2 seconds
    @Test public void allEpdExamplesToDepthOfThree() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(3, perftRecords);
    }

    // after magic bitboards - 20 seconds.
    // after more optimizations - 6.5 seconds
    // 5.5 seconds (34595 nodes/second) - but why so slow?  35% of execution time spent in JVM setup! (1/7/24)
    // 4 seconds
    @Test public void allEpdExamplesToDepthOfFour() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(4, perftRecords);
    }

    // about 2 seconds
    @Test public void startingPositionDepthFour() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(4, defaultBoard());
    }

    /*
     NOTE - this method runs faster when the whole test suite is run.

     6d1e0ec - creating a new game for each depth traversal - 1082 nodes/second
     45941 ms and reviewed 4867157 nodes.  105943 nodes/second - added undo to replace cloning the game object to save state. - 1fd28a9
     about 30 seconds with Board.get caching
     about 16 seconds with Piece Cache
     about 14.5 seconds with new Rook moves.
     about 12.5 seconds with all pieces using more bit friendly calcs.
     about 4.1 seconds with the new generateMovesFor logic that uses bitboards to verify if a piece is in check.
     5739 ms and reviewed 4865609 nodes.  836,446 nodes/second (896,886 nodes/second, 824,539 nodes/second)
     4703 ms and reviewed 4865609 nodes.  1034575 nodes/second (1,019,616 nodes/second, 1,128,911 nodes/second) - use ints to represent castling
     3718 ms and reviewed 4865609 nodes.  1308662 nodes/second (1,437,402 and 1,270,725) - en passant uses ints.
     3269 ms and reviewed 4865609 nodes.  1488408 nodes/second - simplified game clock counter (1,433,169 and 1,315,740)
     2454 ms and reviewed 4865609 nodes.  1982725 nodes/second - incremental zobrist key management.
     2317 ms and reviewed 4865609 nodes.  2099960 nodes/second - use clone instead of Arrays.copyOf for Board.getCopyOfBoards
     */
    @Test public void startingPositionDepthFive() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(5, defaultBoard());
    }

    @Test public void startingPositionDepthFiveSimple() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        simplifiedDepthTest(5, FenString.INITIAL_BOARD, 4865609);
    }

    @Test public void startingPositionDepthSixSimple() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        simplifiedDepthTest(6, FenString.INITIAL_BOARD, 119060324);
    }

    // ran for 910498 ms and reviewed 3195901860 nodes.  3510059 nodes/second (within IntelliJ, which is slower than on the command line).
    @Test public void startingPositionDepthSevenSimple() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        simplifiedDepthTest(7, FenString.INITIAL_BOARD, 3195901860L);
    }

    // Nodes searched: 119060324
    // 11 mins 41 seconds (about 170 nodes per second)
    // 101805ms => 101 seconds for 119,060,324 nodes => 1,169,493 nodes per second
    // ran for 92295 ms and reviewed 119060324 nodes.  1289997 nodes/second - after en passant and game clock changes
    // ran for 61093 ms and reviewed 119060324 nodes.  1948837 nodes/second - after incremental zobrist hashing update
    // ran for 30357 ms and reviewed 119060324 nodes.  3922005 nodes/second - improved perft to not process depth 1 moves.
    @Test @Disabled
    public void startingPositionDepthSix() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(6, defaultBoard());
    }

    @Test public void testContextBetweenGames() {
        SortedMap<String, Long> actual;

        // uci
        // position startpos
        // go perft 5
        Game depth0 = new Game();
        assertThat(depth0.perftAtRoot(5).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(4865609);

        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -" moves b7b5
        // go perft 1
        Game depth4 = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(depth4.perftAtRoot(1).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(22);


        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/8/P7/8/1PPPPPPP/RNBQKBNR w KQkq -" moves a4a5
        // go perft 2
        Game depth3 = new Game("rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -");
        actual = depth3.perftAtRoot(2);
        assertThat(actual.get("b7b5")).isEqualTo(22);  // stockfish reports 22
        assertThat(actual.values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(380);

        // ucinewgame
        // position fen "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq -" moves a7a6
        // go perft 3
        Game depth2 = new Game("rnbqkbnr/1ppppppp/p7/8/P7/8/1PPPPPPP/RNBQKBNR w KQkq -");
        assertThat(depth2.perftAtRoot(3).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(9312);

        Game temp2 = new Game("rnbqkbnr/2pppppp/pP6/8/8/8/1PPPPPPP/RNBQKBNR b KQkq -");
        assertThat(temp2.perftAtRoot(1).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(19);

        // ucinewgame
        // position fen rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1 moves b7b5 a5b6
        // go perft 1
        Game temp = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(22);
        assertThat(temp.perftAtRoot(2).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(398);  // a5b6 for me is 20, for stockfish it is 19
        assertThat(temp.perftAtRoot(3).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(9432);

        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -" moves a2a4
        // go perft 4
        Game depth1 = new Game("rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1");
        actual = depth1.perftAtRoot(4);
        assertThat(actual.get("b7b5")).isEqualTo(9432);  // stockfish reports 9432, my engine 9456
        assertThat(actual.values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(186257L);
    }

    @Test void returnSameResultsWhenRunTwiceInARow() {
        // ucinewgame
        // position fen rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1 moves b7b5 a5b6
        // go perft 1
        Game temp = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(22);
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(22);
    }

    @Disabled
    @Test void harness() {
        int depth = 7;
        long expected = 3195901860L;

        Game game = new Game(FenString.INITIAL_BOARD);
        game.moveLongNotation("a2a3");  // white move - 106743106 - depth 6
        depth--;
        expected = 106743106;  // I think actual will be 106743106;

        game.moveLongNotation("d7d5");  // black move - 106743106 - depth 5
        depth--;
        expected = 7937327;  // I think actual will be 7937386;

        game.moveLongNotation("a1a2");  // white move - 330675 - depth 4
        depth--;
        expected = 330672;  // I think actual will be 7937386;

        game.moveLongNotation("c8h3");  // black move - 330675 - depth 3
        depth--;
        expected = 10896;  // I think actual will be 10899;

        game.moveLongNotation("g1h3");  // white move - 330675 - depth 2
        depth--;
        expected = 455;  // I think actual will be 456;

        game.moveLongNotation("d8d6");  // black move - 330675 - depth 1
        depth--;
        expected = 19;  // I think actual will be 20 - continans e1c8!!! white king to black rook with blockers!

        SortedMap<String, Long> actual = game.perftAtRoot(depth);

        game.printPerft(actual);

        LOGGER.info(
            "\n" +
                "uci\n" +
                "position fen " + game.asFen() + "\n" +
                "go perft " + depth + "\n" +
                "d\n");

        assertThat(actual.values()
            .stream()
            .reduce(0L, Long::sum)).isEqualTo(expected);
    }

    private void depthHelper(int depth, ArrayList<PerftRecord> perftRecords) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        SortedMap<String, Long> actual;
        String profile = "EMPTY";
        String methodName = "depthOf" + depth;
        long startTime = System.nanoTime();
        long endTime;
        long perftCount = -1;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);

            for(PerftRecord pr : perftRecords) {
                // LOGGER.info("{}: {}", counter++, pr.FenString());
                Game game = new Game(pr.FenString());
                actual = game.perftAtRoot(depth);
                perftCount = actual.values()
                    .stream()
                    .reduce(0L, Long::sum);

                Method expectedDepthMethod = PerftRecord.class.getMethod("d" + depth);
                Integer expected = (Integer) expectedDepthMethod.invoke(pr);
                // LOGGER.info("pulling expected out of {}", pr);
                if(perftCount != expected) {
                    game.printPerft(actual);

                    LOGGER.info(
                        "\n" +
                        "uci\n" +
                        "position fen " + game.asFen() + "\n" +
                        "go perft " + depth + "\n" +
                        "d\n");

                }

                assertThat(perftCount).as("FenString '%s' of depth %d", pr.FenString(), depth).isEqualTo(expected);
            }

            profile = asyncProfiler.dumpFlat(100);
        } finally {
            asyncProfiler.execute("stop,file=./profile/profile" + methodName + "-" + startTime + ".html");
            LOGGER.debug(profile);
        }

        endTime = System.nanoTime();
        long timeDiff = endTime - startTime;
        if(timeDiff == 0) timeDiff = 1; // eliminate the divided by 0 error that occasionally pops up below.

        LOGGER.info("profile{}-{}.html: ran for {} ms and reviewed {} nodes.  {} nodes/second",
            methodName,
            startTime,
            TimeUnit.NANOSECONDS.toMillis(timeDiff),
            perftCount,
            (perftCount * 1000L) / TimeUnit.NANOSECONDS.toMillis(timeDiff)
            );
    }

    private void simplifiedDepthTest(int depth, String fen, long expected) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        SortedMap<String, Long> actual;
        String profile = "EMPTY";
        String methodName = "depthOf" + depth;
        long startTime = System.nanoTime();
        long endTime;
        long perftCount = -1;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);

            // LOGGER.info("{}: {}", counter++, pr.FenString());
            Game game = new Game(fen);
            actual = game.perftAtRoot(depth);
            perftCount = actual.values()
                .stream()
                .reduce(0L, Long::sum);

            if(perftCount != expected) {
                game.printPerft(actual);

                LOGGER.info(
                    "\n" +
                        "uci\n" +
                        "position fen " + game.asFen() + "\n" +
                        "go perft " + depth + "\n" +
                        "d\n");

            }

            profile = asyncProfiler.dumpFlat(100);
        } finally {
            asyncProfiler.execute("stop,file=./profile/profile" + methodName + "-" + startTime + ".html");
            LOGGER.debug(profile);
        }

        endTime = System.nanoTime();
        long timeDiff = endTime - startTime;
        if(timeDiff == 0) timeDiff = 1; // eliminate the divided by 0 error that occasionally pops up below.

        LOGGER.info("profile{}-{}.html: ran for {} ms and reviewed {} nodes.  {} nodes/second",
            methodName,
            startTime,
            TimeUnit.NANOSECONDS.toMillis(timeDiff),
            perftCount,
            (perftCount * 1000L) / TimeUnit.NANOSECONDS.toMillis(timeDiff)
        );
    }
}
