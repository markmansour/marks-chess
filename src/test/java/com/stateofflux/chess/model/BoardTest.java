package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
public class BoardTest {
  @Test
  public void initialBoardAsFenString() {
    Board b = new Board();
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void standardBoardWithFenString() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test public void illegalFenBoardNotEnoughPiecesInRow() {
    assertThrows(IllegalArgumentException.class,
        () -> new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPP/RNBQKBNR", PlayerColor.WHITE)
    ); // only 7P on rank 2
  }

  @Test
  public void emptyBoard() {
    Board b = new Board("", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Blank board")
        .isEqualTo("8/8/8/8/8/8/8/8");
  }

  @Test
  public void boardWithEmptyA1() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/1NBQKBNR", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Empty A1")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/1NBQKBNR");
  }

  @Test
  public void boardWithEmptyH8() {
    Board b = new Board("rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Empty H8")
        .isEqualTo("rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
  }

  @Test
  public void boardWithHalfRowEmptyStart() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/4PPPP/RNBQKBNR", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Empty start of row")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/4PPPP/RNBQKBNR");
  }

  @Test
  public void boardWithHalfRowEmptyEnd() {
    Board b = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/RNBQKBNR", PlayerColor.WHITE);
    assertThat(b.toFen())
        .as("Empty end of row")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPP4/RNBQKBNR");
  }

  // ---------------------- Moving -------------------------------------
  @Test
  public void moveKing() {
    Game game = new Game();
    game.moveLongNotation("e2e4");
    game.moveLongNotation("e7e5");
    game.moveLongNotation("e1e2");

    assertThat(game.asFen())
        .as("Move King")
        .isEqualTo("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b kq - 1 1");
  }

  @Test
  public void moveQueen() {
    Game game = new Game();
    game.moveLongNotation("c2c3");
    game.moveLongNotation("c7c5");
    game.moveLongNotation("d1a4");

    assertThat(game.asFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pp1ppppp/8/2p5/Q7/2P5/PP1PPPPP/RNB1KBNR b KQkq - 1 1");
  }

  @Test
  public void moveRook() {
    Game game = new Game();
    game.moveLongNotation("a2a4");
    game.moveLongNotation("a7a5");
    game.moveLongNotation("a1a3");

    assertThat(game.asFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/1ppppppp/8/p7/P7/R7/1PPPPPPP/1NBQKBNR b Kkq - 1 1");
  }

  @Test
  public void moveBishop() {
    Game game = new Game();
    game.moveLongNotation("b2b3");
    game.moveLongNotation("a7a5");
    game.moveLongNotation("c1a3");

    assertThat(game.asFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/1ppppppp/8/p7/8/BP6/P1PPPPPP/RN1QKBNR b KQkq - 1 1");
  }

  @Test
  public void moveKnight() {
    Game game = new Game();
    game.moveLongNotation("b1a3");

    assertThat(game.asFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/8/N7/PPPPPPPP/R1BQKBNR b KQkq - 1 0");
  }

  @Test
  public void movePawn() {
    Game game = new Game();
    game.moveLongNotation("a2a4");

    assertThat(game.asFen())
        .as("Initial board setup")
        .isEqualTo("rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq - 1 0");
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

  @Test void printMethods() {
    Game game = new Game();
    Board.printOccupied(game.getBoard().getOccupied());
  }

  @Nested
  class RemovedPiece {
    @Test void capturePawn() {
      Game game = new Game("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -");
      MoveList<Move> moves = game.generateMoves();
      Move move = moves.stream().filter(Move::isCapture).findFirst().get();
      game.move(move);
      assertThat(game.asFen()).isEqualTo("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR b KQkq - 1 1");
      assertThat(move.getCapturePiece()).isEqualTo(Piece.BLACK_PAWN);
    }

    @Test void enPassant() {
      Game game = new Game("2r3k1/1q1nbppp/r3p3/3pP3/p1pP4/P1Q2N2/1PRN1PPP/2R4K w - - 0 22");
      game.move("b4"); // from b2 to b4 - creating an en passant situation
      Move m = game.sanToMove("cxb3"); // black pawn en passant take remove b4
      game.move(m);
      assertThat(m.isCapture()).isTrue();
      assertThat(m.getCapturePiece()).isEqualTo(Piece.WHITE_PAWN);
    }
  }
}
