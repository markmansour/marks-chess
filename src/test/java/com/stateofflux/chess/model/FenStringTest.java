package com.stateofflux.chess.model;

import org.testng.annotations.*;
import static org.assertj.core.api.Assertions.*;

public class FenStringTest {
    @BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
    }

    @Test
    public void locationToAlgebraString() {
        assertThat(FenString.locationToSquare(0)).isEqualTo("a1");
        assertThat(FenString.locationToSquare(63)).isEqualTo("h8");
        assertThatThrownBy(() -> { FenString.locationToSquare(-1); }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void algebraStringToLocation() {
        assertThat(FenString.squareToLocation("a1")).isEqualTo(0);
        assertThat(FenString.squareToLocation("h8")).isEqualTo(63);
        assertThatThrownBy(() -> { FenString.squareToLocation("a9"); }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void activePlayerColor() {
        FenString fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -");
        assertThat(fs.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq -");
        assertThat(fs.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);

        assertThatThrownBy(() -> { new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq -"); }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setCastlingRights() {
        FenString fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -");
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder(new char[] {'K', 'Q', 'k', 'q'});

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQk -");
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder(new char[] {'K', 'Q', 'k'});

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w kQK -");
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder(new char[] {'K', 'Q', 'k'});

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - -");
        assertThat(fs.getCastlingRights()).isEqualTo("-");

        assertThatThrownBy(() -> { new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KPkq -"); }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setEnPassantTarget() {
        FenString fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -");
        assertThat(fs.getEnPassantTarget()).isEqualTo("-");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a3");
        assertThat(fs.getEnPassantTarget()).isEqualTo("a3");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq h6");
        assertThat(fs.getEnPassantTarget()).isEqualTo("h6");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a6");
        assertThat(fs.getEnPassantTarget()).isEqualTo("a6");

        assertThatThrownBy(() -> { new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq i4"); }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> { new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a4"); }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void moveClocks() {
        FenString fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
        assertThat(fs.getHalfmoveClock()).isZero();
        assertThat(fs.getFullmoveCounter()).isZero();

        fs = new FenString("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 5 10");
        assertThat(fs.getHalfmoveClock()).isEqualTo(5);
        assertThat(fs.getFullmoveCounter()).isEqualTo(10);
    }
}
