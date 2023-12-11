package com.stateofflux.chess.perft;

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

    // 3 runs from IntelliJ - 793ms, 770ms, 921ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 208ms, 207ms, 20ms
    @Test public void firstRecordDepthOne() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        perftRecords.subList(1,perftRecords.size()).clear();  // keep only the first item.
        depthHelper(1);
    }

    // 3 runs from IntelliJ - 627ms, 592ms, 529ms
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 271ms, 207ms, 203ms
    @Test public void firstRecordDepthTwo() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        perftRecords.subList(1,perftRecords.size()).clear();  // keep only the first item.
        depthHelper(2);
    }

    // 3 runs from IntelliJ - 15s 527ms, 14s 179ms, 13s 594ms.
    // Stopped using constructor Game(String FenString) and instead used Game(Game g)
    // 6 sec 311 ms, 5 sec 464 ms, 4 sec 809ms
    @Test public void firstRecordDepthThree() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        perftRecords.subList(1,perftRecords.size()).clear();  // keep only the first item.
        depthHelper(3);
    }

    // 1.4 seconds
    @Test public void depthOfOne() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        depthHelper(1);
    }

    // 11 seconds
    @Test public void depthOfTwo() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        depthHelper(2);
    }

    // 3 mins 41 seconds
    @Test public void depthOfThree() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        depthHelper(3);
    }

    private void depthHelper(int depth) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
                actual = game.perftAtRoot(game, depth);
                int perftCount = actual.values()
                    .stream()
                    .reduce(0, Integer::sum);

                Method expectedDepthMethod = PerftRecord.class.getMethod("d" + depth);
                Integer expected = (Integer) expectedDepthMethod.invoke(pr, null);

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
            asyncProfiler.execute("stop,file=./profile/profile" + methodName + "-" + startTime + ".html");
        } catch (IOException ioe) {
            LOGGER.error("IOException " + ioe);
        } finally {
            LOGGER.info(profile);
        }

        endTime = System.nanoTime();
        LOGGER.info("Ran for: {} nanoseconds", TimeUnit.NANOSECONDS.toNanos(endTime - startTime));
    }
}
