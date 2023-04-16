package com.stateofflux.chess.model;

import org.testng.annotations.*;
import static org.assertj.core.api.Assertions.*;

public class BoardTest {
  @BeforeClass
  public void setUp() {
    // code that will be invoked when this test is instantiated
  }

  @Test
  public void initialBoardAsFenString() {
    Board b = new Board();
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void StandardBoardWithFenStringg() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void emptyBoard() {
    Board b = new Board("");
    assertThat(b.toFenString())
        .as("Blank board")
        .isEqualTo("8/8/8/8/8/8/8/8");
  }

  @Test
  public void boardWithEmptyA1() {
    Board b = new Board("1nbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFenString())
        .as("Empty A1")
        .isEqualTo("1nbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void boardWithEmptyH8() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1");
    assertThat(b.toFenString())
        .as("Empty H8")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1");
  }

  @Test
  public void boardWithHalfRowEmptyStart() {
    Board b = new Board("rnbqkbnr/4pppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFenString())
        .as("Empty start of row")
        .isEqualTo("rnbqkbnr/4pppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void boardWithHalfRowEmptyEnd() {
    Board b = new Board("rnbqkbnr/pppp4/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFenString())
        .as("Empty end of row")
        .isEqualTo("rnbqkbnr/pppp4/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

}
