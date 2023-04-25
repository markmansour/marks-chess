package com.stateofflux.chess.model;

import org.testng.annotations.*;

public class PlayerTest {
    @BeforeClass
    public void setUp() {
      // code that will be invoked when this test is instantiated
    }

    @Test
    public void aTest() {
        Board b = new Board();
        Player p = new Player(b, PlayerColor.WHITE);
    }
}
