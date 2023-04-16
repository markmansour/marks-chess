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

  // ---------------------- Moving -------------------------------------
  @Test
  public void moveKing() {
    Board b = new Board();
    b.move("e2", "e4");
    b.move("e1", "e2");
    assertThat(b.toFenString())
        .as("Move King")
        .isEqualTo("rnbq1bnr/ppppkppp/8/4p3/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void moveQueen() {
    Board b = new Board();
    b.move("d2", "d3");  // move pawn up
    b.move("d1", "a4");  // move queen NW 4 places
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("rnb1kbnr/ppp1pppp/3p4/q7/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void moveRook() {
    Board b = new Board();
    b.move("a2", "a4");
    b.move("a1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("1nbqkbnr/1ppppppp/r7/p7/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void moveBishop() {
    Board b = new Board();
    b.move("b2", "b3");  // move pawn up
    b.move("c1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("rn1qkbnr/p1pppppp/bp6/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void moveKnight() {
    Board b = new Board();
    b.move("b1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("r1bqkbnr/pppppppp/n7/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void movePawn() {
    Board b = new Board();
    b.move("a2", "a4");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/1ppppppp/8/p7/8/8/PPPPPPPP/RNBQKBNR");
  }


  // ---------------------- Utility functions --------------------------
  @Test
  public void convertA1toIndex() {
    assertThat(Board.convertStringToIndex("a1"))
        .isEqualTo(0);
  }

  @Test
  public void convertH8toIndex() {
    assertThat(Board.convertStringToIndex("h8"))
        .isEqualTo(63);
  }
}
