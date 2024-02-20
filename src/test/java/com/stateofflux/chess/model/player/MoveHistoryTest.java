package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoveHistoryTest {
    @Test public void basic() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);
        assertThat(mh.bestMove().toString()).isEqualTo("P : a2a3");
        assertThat(mh.score()).isEqualTo(100);
        assertThat(mh.previousMoves()).hasSize(0);
    }

    @Test public void haveHistoryOfOne() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);
        MoveHistory mhNew = mh.createNew(new Move(Piece.WHITE_ROOK, 0, 8, false), 200);

        assertThat(mhNew.bestMove().toString()).isEqualTo("R : a1a2");
        assertThat(mhNew.score()).isEqualTo(200);
        assertThat(mhNew.previousMoves()).hasSize(1);
        assertThat(mhNew.previousMovesToString()).isEqualTo("a2a3");
    }

    @Test public void haveHistoryOfTwo() {
        MoveHistory mh = new MoveHistory(new Move(Piece.WHITE_PAWN, 8, 16, false), 100);
        MoveHistory mhTemp = mh.createNew(new Move(Piece.WHITE_ROOK, 0, 8, false), 200);
        MoveHistory mhNew = mhTemp.createNew(new Move(Piece.WHITE_BISHOP, 2, 11, false), 300);

        assertThat(mhNew.bestMove().toString()).isEqualTo("B : c1d2");
        assertThat(mhNew.score()).isEqualTo(300);
        assertThat(mhNew.previousMoves()).hasSize(2);
        assertThat(mhNew.previousMovesToString()).isEqualTo("a2a3 a1a2");
    }
}