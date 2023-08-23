package com.stateofflux.chess.model;

import org.testng.annotations.Test;

import com.stateofflux.chess.model.pieces.PieceMoves;
import com.stateofflux.chess.model.pieces.KingMoves;

import static org.assertj.core.api.Assertions.*;

public class KingMovesTest {
    @Test
    public void openingOptions() {
        Game game = new Game();

        // starting at position 8, moving up 2 squares should give an answer of 16 | 24.
        PieceMoves pieceMoves = new KingMoves(game.getBoard(), 4);

        assertThat(pieceMoves.getNonCaptureMoves()).isZero();
        assertThat(pieceMoves.getCaptureMoves()).isZero();
    }

    @Test
    public void movingUpIntoEmptySpace() {
        Game game = new Game("rnbqkbnr/pppp4/4pppp/1B6/3PPPQ1/8/PPP3PP/RNB1K1NR b KQkq -");
        PieceMoves pieceMoves = new KingMoves(game.getBoard(), 4);

        assertThat(pieceMoves.getNonCaptureMoves()).isEqualTo(1 << 3L | 1L << 11 | 1L << 12 | 1L << 13 | 1L << 5);
        assertThat(pieceMoves.getCaptureMoves()).isZero();
    }

    public class Castling {
        @Test
        public void adsadf() {
            Game game = new Game("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/R3KBNR w KQkq -");
        }
    }
}
