package com.stateofflux.chess.perft;

import com.stateofflux.chess.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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
        PerftRecord first = perftRecords.get(0);
        Game game = new Game(first.FenString());
        assertThat(game.perft(first.FenString(), 1)).isEqualTo(first.d1());
    }

    @Test public void depthOfOne() {
        for(PerftRecord pr : perftRecords) {
            Game game = new Game(pr.FenString());
            assertThat(game.perft(pr.FenString(), 1)).as("FenString '%s' of depth 1", pr.FenString()).isEqualTo(pr.d1());
        }
    }
}
