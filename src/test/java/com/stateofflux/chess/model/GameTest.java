package com.stateofflux.chess.model;

import org.testng.annotations.*;

import static org.assertj.core.api.Assertions.*;

public class GameTest {
    @BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
    }

    @Test
    public void scanInitialFenString() {

        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
        Game g = new Game(fen);
        assertThat(g.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertThat(g.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(g.getCastlingRights()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(new String(g.getEnPassantTarget())).isEqualTo("-");
        assertThat(g.getHalfmoveClock()).isZero();
        assertThat(g.getFullmoveCounter()).isEqualTo(1);
    }
}
