package com.stateofflux.chess.model;

import org.testng.annotations.*;
import static org.assertj.core.api.Assertions.*;

public class GameTest {
  @BeforeClass
  public void setUp() {
    // code that will be invoked when this test is instantiated
  }

  @Test
  public void aTest() {
    Game g = new Game();

    assertThat(g.white.validMoves("a7"))
        .isNotEmpty()
        .hasSize(2)
        .containsExactly("a7a6", "a7a5");
  }
}
