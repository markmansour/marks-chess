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

public class DepthTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepthTest.class);
    record PerftRecord(String FenString, int d1, int d2, int d3, int d4, int d5, int d6) {};
;
    private ArrayList<PerftRecord> perftRecords;

    @BeforeSuite
    public void setUp() {
        String resourceName = "./perftsuite.epd";
        perftRecords = new ArrayList<>();

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

/*

    @Test public void testFirstItem() {
        fail("todo");

        try {
            long startTime = System.nanoTime();
            long endTime;
            AsyncProfiler.getInstance().start(Events.CPU, 1_000_000);

            PerftRecord first = perftRecords.get(0);
            Game game = new Game(first.FenString());
//            assertThat(game.perft(first.FenString(), 1)).isEqualTo(first.d1());
//            assertThat(game.perft(first.FenString(), 2)).isEqualTo(first.d2());
            assertThat(game.perft(game, 3)).isEqualTo(first.d3());

            String profile = AsyncProfiler.getInstance().dumpFlat(100);
            System.out.println(profile);

            AsyncProfiler.getInstance().execute("stop,file=profile2.html");
            endTime = System.nanoTime();
            LOGGER.info("Ran for: {} seconds", TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
        } catch(IOException e) {
            LOGGER.error("IOException " + e);
        }

    }
*/

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
        SortedMap<String, Integer> perftResults;
        int counter = 1;

        for(PerftRecord pr : perftRecords) {
            LOGGER.info("{}: {}", counter++, pr.FenString());
            Game game = new Game(pr.FenString());
            perftResults = game.perftAtRoot(game, depth);
            int perftCount = perftResults.values()
                .stream()
                .reduce(0, Integer::sum);

            Method expectedDepthMethod = PerftRecord.class.getMethod("d" + depth);
            Integer actual = (Integer) expectedDepthMethod.invoke(pr, null);

            if(perftCount != actual) {
                game.printPerft(perftResults);

                LOGGER.info(
                    "\n" +
                    "uci\n" +
                    "position fen " + game.asFen() + "\n" +
                    "go perft " + depth + "\n" +
                    "d\n");

            }

            assertThat(perftCount).as("FenString '%s' of depth %d", pr.FenString(), depth).isEqualTo(actual);
        }
    }
}
