package com.stateofflux.chess.perft;

import com.stateofflux.chess.model.FenString;
import one.profiler.AsyncProfiler;
import one.profiler.Events;

import com.stateofflux.chess.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/*
 * See JMH repo for examples on how to use the profiler:
 * https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples
 *
 * TODO: Instead of using Unit Test annotations to run performance tests, conver this to
 *       JMH annotations.
 * TODO: Move from the unit test package to a performance test package.
 * TODO: Utilize IntelliJ plugin - https://github.com/artyushov/idea-jmh-plugin
 */
public class DepthTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepthTest.class);
    record PerftRecord(String FenString, int d1, int d2, int d3, int d4, int d5, int d6) {};

    private ArrayList<PerftRecord> perftRecords;
    AsyncProfiler asyncProfiler;

    @BeforeSuite
    public void setUp() {
        // from http://www.rocechess.ch/perft.html
        // see Perft-testsuite - http://www.rocechess.ch/perftsuite.zip
        // 126 test cases
        String resourceName = "./perftsuite.epd";

        perftRecords = new ArrayList<>();
        asyncProfiler = AsyncProfiler.getInstance();

        try(InputStream contents = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assert contents != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
            String text = reader.readLine();
            while(text != null) {
                Pattern p = Pattern.compile("(.+);D1 (\\d+) ;D2 (\\d+) ;D3 (\\d+) ;D4 (\\d+) ;D5 (\\d+) ;D6 (\\d+)");
                Matcher m = p.matcher(text);
                if(m.matches()) {
                    perftRecords.add(
                        new PerftRecord(
                            m.group(1),
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
            LOGGER.info(text);
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
/*

    @Test public void debugPerft() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq -",
            20,
            0,0,0,0,0
        ));
        depthHelper(1, list);
    }

    @Test public void debugPerft2() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "rnbqkbnr/pppppppp/8/8/8/1P6/P1PPPPPP/RNBQKBNR b KQkq -",
            0,
            0,9345,0,0,0
        ));
        depthHelper(3, list);
    }

    @Test public void debugPerft3() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "rnbqkbnr/pppp1ppp/8/4p3/8/1P6/P1PPPPPP/RNBQKBNR w KQkq -",
            0,
            629,0,0,0,0
        ));
        depthHelper(2, list);
    }

    @Test public void debugPerft4() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "rnbqkbnr/pppp1ppp/8/4p3/8/BP6/P1PPPPPP/RN1QKBNR b KQkq -",
            29,
            0,0,0,0, 0
        ));
        depthHelper(1, list);
    }

    @Test public void debugPerft5() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
            0,
            568,0,0,0, 0
        ));
        depthHelper(2, list);
    }
*/

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

/*
    // TAKES A LONG TIME TO RUN
    // 33 seconds => 4867157 nodes ~= 146 nodes per ms
    @Test public void firstRecordDepthFive() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(5, perftRecordsSizeOne());

    }
*/


    // 1.4 seconds
    @Test public void allEpdExamplesToDepthOfOne() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(1, perftRecords);
    }

    // 11 seconds
    @Test public void allEpdExamplesToDepthOfTwo() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(2, perftRecords);
    }

    // 3 mins 41 seconds
    @Test public void allEpdExamplesToDepthOfThree() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        fail("too long");
        depthHelper(3, perftRecords);
    }

    @Test public void allEpdExamplesToDepthOfFour() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
//        depthHelper(4, perftRecords);
        fail("too long");
    }

    // about 2 seconds
    @Test public void startingPositionDepthFour() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        depthHelper(4, defaultBoard());
    }

    // about 43 seconds
    // about 30 seconds with Board.get caching
    @Test public void startingPositionDepthFive() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        fail("too long");
        depthHelper(5, defaultBoard());
    }

    // Nodes searched: 119060324
    // 11 mins 41 seconds (about 170 nodes per second)
    @Test public void startingPositionDepthSix() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        fail("too long");
        depthHelper(6, defaultBoard());
    }


    /*@Test public void debugPerft6() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<PerftRecord> list = new ArrayList<>();
        list.add(new PerftRecord(
            "rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -",
            0,
            568,0,0,0, 0
        ));
        depthHelper(2, list);
    }*/

    @Test public void testContextBetweenGames() {
        fail("too long");
        SortedMap<String, Integer> actual;

        // uci
        // position startpos
        // go perft 5
        Game depth0 = new Game();
        assertThat(depth0.perftAtRoot(5).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(4865609);

        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -" moves b7b5
        // go perft 1
        Game depth4 = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(depth4.perftAtRoot(1).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(22);


        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/8/P7/8/1PPPPPPP/RNBQKBNR w KQkq -" moves a4a5
        // go perft 2
        Game depth3 = new Game("rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -");
        actual = depth3.perftAtRoot(2);
        assertThat(actual.get("b7b5")).isEqualTo(22);  // stockfish reports 22
        assertThat(actual.values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(380);

        // ucinewgame
        // position fen "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq -" moves a7a6
        // go perft 3
        Game depth2 = new Game("rnbqkbnr/1ppppppp/p7/8/P7/8/1PPPPPPP/RNBQKBNR w KQkq -");
        assertThat(depth2.perftAtRoot(3).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(9312);

        Game temp2 = new Game("rnbqkbnr/2pppppp/pP6/8/8/8/1PPPPPPP/RNBQKBNR b KQkq -");
        assertThat(temp2.perftAtRoot(1).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(19);

        // ucinewgame
        // position fen rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1 moves b7b5 a5b6
        // go perft 1
        Game temp = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(22);
        assertThat(temp.perftAtRoot(2).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(398);  // a5b6 for me is 20, for stockfish it is 19
        assertThat(temp.perftAtRoot(3).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(9432);

        // ucinewgame
        // position fen "rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq -" moves a2a4
        // go perft 4
        Game depth1 = new Game("rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1");
        actual = depth1.perftAtRoot(4);
        assertThat(actual.get("b7b5")).isEqualTo(9432);  // stockfish reports 9432, my engine 9456
        assertThat(actual.values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(186257);
    }

    @Test void returnSameResultsWhenRunTwiceInARow() {
        // ucinewgame
        // position fen rnbqkbnr/1ppppppp/p7/P7/8/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1 moves b7b5 a5b6
        // go perft 1
        Game temp = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(22);
        assertThat(temp.perftAtRoot(1).values()
            .stream()
            .reduce(0, Integer::sum)).isEqualTo(22);
    }

    private void depthHelper(int depth, ArrayList<PerftRecord> perftRecords) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        SortedMap<String, Integer> actual;
        int counter = 1;
        String profile = "EMPTY";
        String methodName = "depthOf" + depth;
        long startTime = System.nanoTime();
        long endTime;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);

            for(PerftRecord pr : perftRecords) {
                LOGGER.info("{}: {}", counter++, pr.FenString());
                Game game = new Game(pr.FenString());
                actual = game.perftAtRoot(depth);
                int perftCount = actual.values()
                    .stream()
                    .reduce(0, Integer::sum);

                Method expectedDepthMethod = PerftRecord.class.getMethod("d" + depth);
                Integer expected = (Integer) expectedDepthMethod.invoke(pr, null);
                LOGGER.info("pulling expected out of {}", pr);
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
            LOGGER.info(profile);
        }

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }
}
