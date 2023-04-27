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
        .isEqualTo("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void standardBoardWithFenString() {
    Board b = new Board("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void IllegalFenBoardNotEnoughPiecesInRow() {
    new Board("RNBQKBNR/PPPPPPP/8/8/8/8/pppppppp/rnbqkbnr"); // only 7P on rank 2
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
    Board b = new Board("1NBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
    assertThat(b.toFenString())
        .as("Empty A1")
        .isEqualTo("1NBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void boardWithEmptyH8() {
    Board b = new Board("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbn1");
    assertThat(b.toFenString())
        .as("Empty H8")
        .isEqualTo("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbn1");
  }

  @Test
  public void boardWithHalfRowEmptyStart() {
    Board b = new Board("RNBQKBNR/4PPPP/8/8/8/8/pppppppp/rnbqkbnr");
    assertThat(b.toFenString())
        .as("Empty start of row")
        .isEqualTo("RNBQKBNR/4PPPP/8/8/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void boardWithHalfRowEmptyEnd() {
    Board b = new Board("RNBQKBNR/PPPP4/8/8/8/8/pppppppp/rnbqkbnr");
    assertThat(b.toFenString())
        .as("Empty end of row")
        .isEqualTo("RNBQKBNR/PPPP4/8/8/8/8/pppppppp/rnbqkbnr");
  }

  // ---------------------- Moving -------------------------------------
  @Test
  public void moveKing() {
    Board b = new Board();
    b.move("e2", "e4");
    b.move("e1", "e2");
    assertThat(b.toFenString())
        .as("Move King")
        .isEqualTo("RNBQ1BNR/PPPPKPPP/8/4P3/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void moveQueen() {
    Board b = new Board();
    b.move("d2", "d3"); // move pawn up
    b.move("d1", "a4"); // move queen NW 4 places
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("RNB1KBNR/PPP1PPPP/3P4/Q7/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void moveRook() {
    Board b = new Board();
    b.move("a2", "a4");
    b.move("a1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("1NBQKBNR/1PPPPPPP/R7/P7/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void moveBishop() {
    Board b = new Board();
    b.move("b2", "b3"); // move pawn up
    b.move("c1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("RN1QKBNR/P1PPPPPP/BP6/8/8/8/pppppppp/rnbqkbnr");
    b.printBoard();
  }

  @Test
  public void moveKnight() {
    Board b = new Board();
    b.move("b1", "a3");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("R1BQKBNR/PPPPPPPP/N7/8/8/8/pppppppp/rnbqkbnr");
  }

  @Test
  public void movePawn() {
    Board b = new Board();
    b.move("a2", "a4");
    assertThat(b.toFenString())
        .as("Initial board setup")
        .isEqualTo("RNBQKBNR/1PPPPPPP/8/P7/8/8/pppppppp/rnbqkbnr");
  }

  // ---------------------- Utility functions --------------------------
  @Test
  public void convertA1toIndex() {
    assertThat(Board.convertPositionToLocation("a1"))
        .isZero();
  }

  @Test
  public void convertH8toIndex() {
    assertThat(Board.convertPositionToLocation("h8"))
        .isEqualTo(63);
  }

  // ----------------------- Generate Moves --------------------------
  @Test
  public void generateOpeningMoves() {
    Board b = new Board();
    assertThat(b.generateMoves()).isEqualTo(40);
  }
}
