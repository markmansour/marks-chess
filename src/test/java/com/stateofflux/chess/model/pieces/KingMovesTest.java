package com.stateofflux.chess.model.pieces;

import com.stateofflux.chess.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

public class KingMovesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(KingMovesTest.class);

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

    @Test void isBlackKingInCheck() {
        String[] fens = {
            "3k3r/6bp/1pp1bp2/5p2/p1PQ4/1q6/5PPP/4R1K1 b - -", // white queen
            "8/5k1p/1p2b1PP/4K3/1P6/P7/8/2q5 b - -", // pawn (g6 - 46), king (f7 - 53)
            "3R2k1/1b3pp1/4pn1p/2q1N3/2p2P2/P3R1P1/1P5P/5K2 b - -",  // white rook
            "2bk3r/6bp/1pp1Np2/5p2/p1Pr3Q/1q6/5PPP/4R1K1 b - -", // white knight
        };

        for(String fen : fens) {
            Game game = new Game(fen);
            LOGGER.info(fen);
            assertThat(game.isChecked()).isTrue();
        }
    }

    @Test void isWhiteKingInCheck() {
        String[] fens = {
            "r2q2nr/pppb1kb1/2np3p/3P2p1/4P3/2N2N2/PPP3pP/R1BQ1R1K w - -", // black pawn
            "2rk3r/1p3p2/p4N1p/4pb2/3P3p/P4p2/P5PP/2KR1B1R w - -",  // black rook
            "r1bq1rk1/ppp1p1bp/3p2p1/3P4/2P1P1n1/2Nn4/PP2N1PP/RQB1K2R w KQ -",  // black knight
            "1r1q1rk1/p1p1pp1p/2n3pb/8/1p1PP3/1BP2Q1P/PB3PP1/2KR3R w - -", // black bishop
            "8/5k1P/1p2b1p1/4K3/1P6/P3q3/8/8 w - -", // black queen
        };

        for(String fen : fens) {
            Game game = new Game(fen);
            assertThat(game.isChecked()).isTrue();
        }
    }

    @Test void postCastlingShoudLimitNextMoves() {
        Game game = new Game("4k3/8/8/8/8/8/8/4K2R w K -");
        game.moveLongNotation("e1g1");  // castle king side white
        // black should not be able to move into f file as the castled rook blocks the king from moving into check
        assertThat(game.generateMoves()).hasSize(3);
    }

    @Test void kingCantMoveIntoCheck() {
        Game game = new Game("4k3/8/8/8/8/8/8/5RK1 b - -");
        assertThat(game.generateMoves()).hasSize(3);
    }

    @Test void kingCanMoveNearAnotherKing() {
        Game game = new Game("4k2K/8/8/8/8/8/8/8 b - -");
        assertThat(game.generateMoves().asLongSan()).containsOnly("e8d7", "e8e7", "e8f7", "e8d8", "e8f8");
    }

}
