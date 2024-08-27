package com.stateofflux.chess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class AppTest {
    InputStream sysInBackup;
    PrintStream sysOutBackup;
    OutputStream baos;

    @BeforeEach
    public void backupSystemsInOut() {
        sysInBackup = System.in;
        sysOutBackup = System.out;

        baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
    }

    @AfterEach
    public void restoreDefaultSystemInOut() {
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    @Test public void testUciInterface() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AppArgs aa = new AppArgs();
        App app = new App(aa);

        String data = "uci\n" + "quit\n";
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);

        app.uciLoop();
        String output = new String(((ByteArrayOutputStream) baos).toByteArray());
        assertThat(output).contains("uciok");
    }

    @Test public void isReady() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AppArgs aa = new AppArgs();
        App app = new App(aa);

        String data =
            "uci\n" +
                "isready\n" +
                "quit\n";
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);

        app.uciLoop();
        String output = new String(((ByteArrayOutputStream) baos).toByteArray());
        assertThat(output).contains("readyok");
    }

    @Test public void goWinc() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AppArgs aa = new AppArgs();
        aa.whiteStrategy = "AlphaBetaPlayerWithTT";
        aa.blackStrategy = "AlphaBetaPlayerWithTT";
        App app = new App(aa);

        String data =
            "uci\n" +
                "isready\n" +
                "go wtime 19636 btime 20000 movestogo 40\n" +
                "quit\n";
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);

        app.uciLoop();
        String output = new String(((ByteArrayOutputStream) baos).toByteArray());
        assertThat(output).contains("readyok");
    }


}