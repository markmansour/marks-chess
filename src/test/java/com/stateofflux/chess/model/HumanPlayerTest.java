package com.stateofflux.chess.model;

import org.testng.annotations.*;

public class HumanPlayerTest {
    @BeforeClass
    public void setUp() {
      // code that will be invoked when this test is instantiated
    }

    @Test
    public void moveBasic() {
        Board b = new Board(); // default board
        HumanPlayer p = new HumanPlayer(b, PlayerColor.BLACK);
        p.move("e2", "e4");
    }
}
