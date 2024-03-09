package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class MoveHistoryTest {
    @Test public void basic() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);  // a2a3
        assertThat(mh.bestMove().toString()).isEqualTo("P : a2a3");
        assertThat(mh.score()).isEqualTo(100);
        assertThat(mh.previousMoves()).hasSize(0);
        assertThat(mh.pv()).isEqualTo("a2a3");
    }

    @Test public void haveHistoryOfOne() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);  // a2a3
        mh.addMove(new Move(Piece.WHITE_ROOK, 0, 8, false), 200);  // a1a2

        assertThat(mh.bestMove().toString()).isEqualTo("R : a1a2");
        assertThat(mh.score()).isEqualTo(200);
        assertThat(mh.previousMoves()).hasSize(1);
        assertThat(mh.previousMovesToString()).isEqualTo("a2a3");
        assertThat(mh.pv()).isEqualTo("a1a2 a2a3");
    }

    @Test public void haveHistoryOfTwo() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);  // a2a3
        mh.addMove(new Move(Piece.WHITE_ROOK, 0, 8, false), 200);  // a1a2
        mh.addMove(new Move(Piece.WHITE_BISHOP, 2, 11, false), 300);  // c1d2

        assertThat(mh.bestMove().toString()).isEqualTo("B : c1d2");
        assertThat(mh.score()).isEqualTo(300);
        assertThat(mh.previousMoves()).hasSize(2);
        assertThat(mh.previousMovesToString()).isEqualTo("a1a2 a2a3");
        assertThat(mh.pv()).isEqualTo("c1d2 a1a2 a2a3");
    }
}