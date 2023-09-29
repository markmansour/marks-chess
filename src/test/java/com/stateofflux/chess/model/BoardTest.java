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
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void standardBoardWithFenString() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void IllegalFenBoardNotEnoughPiecesInRow() {
    new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPP/RNBQKBNR"); // only 7P on rank 2
  }

  @Test
  public void emptyBoard() {
    Board b = new Board("");
    assertThat(b.toFen())
        .as("Blank board")
        .isEqualTo("8/8/8/8/8/8/8/8");
  }

  @Test
  public void boardWithEmptyA1() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/1NBQKBNR");
    assertThat(b.toFen())
        .as("Empty A1")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/1NBQKBNR");
  }

  @Test
  public void boardWithEmptyH8() {
    Board b = new Board("rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    assertThat(b.toFen())
        .as("Empty H8")
        .isEqualTo("rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void boardWithHalfRowEmptyStart() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/4PPPP/RNBQKBNR");
    assertThat(b.toFen())
        .as("Empty start of row")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/4PPPP/RNBQKBNR");
  }

  @Test
  public void boardWithHalfRowEmptyEnd() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/RNBQKBNR");
    assertThat(b.toFen())
        .as("Empty end of row")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/RNBQKBNR");
  }

  // ---------------------- Moving -------------------------------------
  @Test
  public void moveKing() {
    Board b = new Board();
    b.move("e2", "e4");
    b.move("e1", "e2");
    assertThat(b.toFen())
        .as("Move King")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPPKPPP/RNBQ1BNR");
  }

  @Test
  public void moveQueen() {
    Board b = new Board();
    b.move("d2", "d3"); // move pawn up
    b.move("d1", "a4"); // move queen NW 4 places
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/Q7/3P4/PPP1PPPP/RNB1KBNR");
  }

  @Test
  public void moveRook() {
    Board b = new Board();
    b.move("a2", "a4");
    b.move("a1", "a3");
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/P7/R7/1PPPPPPP/1NBQKBNR");
  }

  @Test
  public void moveBishop() {
    Board b = new Board();
    b.move("b2", "b3"); // move pawn up
    b.move("c1", "a3");
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/BP6/P1PPPPPP/RN1QKBNR");
    b.printOccupied();
  }

  @Test
  public void moveKnight() {
    Board b = new Board();
    b.move("b1", "a3");
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/N7/PPPPPPPP/R1BQKBNR");
  }

  @Test
  public void movePawn() {
    Board b = new Board();
    b.move("a2", "a4");
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR");
  }

  @Test
  public void testBitboardToArray() {
    assertThat(Board.bitboardToArray(255L << 8)).hasSize(8);
    assertThat(Board.bitboardToArray(255L << 48)).hasSize(8);
    assertThat(Board.bitboardToArray(1L << 57 | 1L << 62)).hasSize(2);
//    assertThat(Board.bitboardToArray(Long.MAX_VALUE)).hasSize(64); // debug this later as I'll never have 64 pieces on the board
  }

  // ---------------------- Utility functions --------------------------
  @Test
  public void convertA1toIndex() {
    assertThat(FenString.squareToLocation("a1"))
        .isZero();
  }

  @Test
  public void convertH8toIndex() {
    assertThat(FenString.squareToLocation("h8"))
        .isEqualTo(63);
  }
}
