package com.stateofflux.chess;

import com.stateofflux.chess.model.Game;
import one.profiler.AsyncProfiler;
import one.profiler.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
public class ProfileTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileTest.class);

    record PerftRecord(String FenString, int d1, int d2, int d3, int d4, int d5, int d6) { };

    private ArrayList<PerftRecord> perftRecords;
    AsyncProfiler asyncProfiler;

    @BeforeSuite
    public void setUp() {
        String resourceName = "./perftsuite.epd";
        perftRecords = new ArrayList<>();
        asyncProfiler = AsyncProfiler.getInstance();

        try (InputStream contents = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assert contents != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
            String text = reader.readLine();
            while (text != null) {
                Pattern p = Pattern.compile("(.+);D1 (\\d+) ;D2 (\\d+) ;D3 (\\d+) ;D4 (\\d+) ;D5 (\\d+) ;D6 (\\d+)");
                Matcher m = p.matcher(text);
                if (m.matches()) {
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
/*

    // 3 runs from IntelliJ - 793ms, 770ms, 921ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 208ms, 207ms, 20ms
    @Test
    public void depthOneTest() {
        boolean started = false;
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        long startTime = System.nanoTime();
        long endTime;
        PerftRecord first = perftRecords.get(0);
        String profile = "EMPTY";
        Game game;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);
            game = new Game(first.FenString());
            assertThat(game.perft(game, 1)).isEqualTo(first.d1());

            profile = asyncProfiler.dumpFlat(100);
        } finally {
            try {
                asyncProfiler.execute("stop,file=profile/profile" + methodName + "-" + startTime + ".html");
                LOGGER.info(profile);
            } catch (IOException ioe) {
                LOGGER.error("IOException " + ioe);
            }

        }

        if(game != null) {
            game.printPerftResults();
        }

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }


    // 3 runs from IntelliJ - 627ms, 592ms, 529ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 271ms, 207ms, 203ms
    @Test
    public void depthTwoTest() {
        boolean started = false;
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        long startTime = System.nanoTime();
        long endTime;
        PerftRecord first = perftRecords.get(0);
        String profile = "EMPTY";
        Game game;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);
            game = new Game(first.FenString());
            assertThat(game.perft(game, 2)).isEqualTo(first.d2());

            profile = asyncProfiler.dumpFlat(100);
        } finally {
            try {
                asyncProfiler.execute("stop,file=profile/profile" + methodName + "-" + startTime + ".html");
                LOGGER.info(profile);
            } catch (IOException ioe) {
                LOGGER.error("IOException " + ioe);
            }

        }

        if(game != null) {
            game.printPerftResults();
        }

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }
*/

    // 3 runs from IntelliJ - 15s 527ms, 14s 179ms, 13s 594ms.
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 6 sec 311 ms, 5 sec 464 ms, 4 sec 809ms
    @Test public void depthThreeTest() {
        boolean started = false;
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        long startTime = System.nanoTime();
        long endTime;
        PerftRecord first = perftRecords.get(0);
        String profile = "EMPTY";
        Game game;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);
            game = new Game(first.FenString());
            int actual = game.perft(game, 3);
            int expected = first.d3();
            profile = asyncProfiler.dumpFlat(100);
            asyncProfiler.execute("stop,file=./profile/profile" + methodName + "-" + startTime + ".html");
            assertThat(actual).isEqualTo(expected);
        } catch (IOException ioe) {
            LOGGER.error("IOException " + ioe);
        } finally {
            LOGGER.info(profile);
        }

/*
        if(game != null) {
            game.printPerft(perftResults);
        }
*/

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }

    // 2 min 22 secs, 1 min 53 sec
/*
    @Test
    public void depthFourTest() {
        boolean started = false;
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        long startTime = System.nanoTime();
        long endTime;
        PerftRecord first = perftRecords.get(0);
        String profile = "EMPTY";
        Game game;

        try {
            asyncProfiler.start(Events.CPU, 1_000_000);
            game = new Game(first.FenString());
            assertThat(game.perft(game, 4)).isEqualTo(first.d4());

            profile = asyncProfiler.dumpFlat(100);
        } finally {
            try {
                asyncProfiler.execute("stop,file=profile/profile" + methodName + "-" + startTime + ".html");
                LOGGER.info(profile);
            } catch (IOException ioe) {
                LOGGER.error("IOException " + ioe);
            }

        }

        if(game != null) {
            game.printPerftResults();
        }

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }
*/
}
