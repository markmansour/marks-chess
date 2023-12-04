package com.stateofflux.chess.perft;

import one.profiler.AsyncProfiler;
import one.profiler.Events;

import com.stateofflux.chess.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DepthOneTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepthOneTest.class);
    record PerftRecord(String FenString, int d1, int d2, int d3, int d4, int d5, int d6) {};
;
    private ArrayList<PerftRecord> perftRecords;

    @BeforeSuite
    public void setUp() {
        String resourceName = "perftsuite.epd";
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

    @Test public void depthOfOne() {
        int perftCount;

        for(PerftRecord pr : perftRecords) {
            Game game = new Game(pr.FenString());
            perftCount = game.perft(game, 1);
            if(perftCount != pr.d1()) {
                game.printPerftResults();
            }
            assertThat(perftCount).as("FenString '%s' of depth 3", pr.FenString()).isEqualTo(pr.d1());
        }
    }

    @Test public void depthOfThree() {
        int perftCount;

        for(PerftRecord pr : perftRecords) {
            Game game = new Game(pr.FenString());
            perftCount = game.perft(game, 3);
            if(perftCount != pr.d3()) {
                game.printPerftResults();
            }
            assertThat(perftCount).as("FenString '%s' of depth 3", pr.FenString()).isEqualTo(pr.d3());
        }
    }
}
