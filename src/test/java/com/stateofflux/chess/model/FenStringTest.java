package com.stateofflux.chess.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
public class FenStringTest {
    @Test
    public void locationToAlgebraString() {
        assertThat(FenString.locationToSquare(0)).isEqualTo("a1");
        assertThat(FenString.locationToSquare(63)).isEqualTo("h8");
        assertThatThrownBy(() -> FenString.locationToSquare(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void algebraStringToLocation() {
        assertThat(FenString.squareToLocation("a1")).isEqualTo(0);
        assertThat(FenString.squareToLocation("h8")).isEqualTo(63);
        assertThatThrownBy(() -> FenString.squareToLocation("a9")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void activePlayerColor() {
        FenString fs = new FenString(FenString.INITIAL_BOARD);
        assertThat(fs.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq -");
        assertThat(fs.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);

        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq -")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setCastlingRights() {
        FenString fs = new FenString(FenString.INITIAL_BOARD);
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQk -");
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k');

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w kQK -");
        assertThat(fs.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k');

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - -");
        assertThat(fs.getCastlingRights()).isEqualTo("-");

        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KPkq -")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setEnPassantTarget() {
        FenString fs = new FenString(FenString.INITIAL_BOARD);
        assertThat(fs.getEnPassantTarget()).isEqualTo("-");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a3");
        assertThat(fs.getEnPassantTarget()).isEqualTo("a3");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq h6");
        assertThat(fs.getEnPassantTarget()).isEqualTo("h6");

        fs = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a6");
        assertThat(fs.getEnPassantTarget()).isEqualTo("a6");

        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq i4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq a4")).isInstanceOf(IllegalArgumentException.class);
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

    // Testing edge cases for location to square conversion
    @Test
    public void locationToSquareEdgeCases() {
        assertThatThrownBy(() -> FenString.locationToSquare(64)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FenString.locationToSquare(-2)).isInstanceOf(IllegalArgumentException.class);
    }

    // Testing edge cases for square to location conversion with additional characters
    @Test
    public void squareToLocationCheckAndCheckmate() {
        assertThat(FenString.squareToLocation("e2+")).isEqualTo(12);
        assertThat(FenString.squareToLocation("e2#")).isEqualTo(12);
        assertThatThrownBy(() -> FenString.squareToLocation("k9+")).isInstanceOf(IllegalArgumentException.class);
    }

    // Testing invalid piece placements
    @Test
    public void invalidPiecePlacements() {
        assertThatThrownBy(() -> new FenString("8/8/8/8/8/8/8/9 w KQkq -")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/7/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -")).isInstanceOf(IllegalArgumentException.class);
    }

    // Testing invalid castling rights scenarios
    @Test
    public void invalidCastlingRights() {
        assertThatThrownBy(() -> new FenString(FenString.INITIAL_BOARD.replace("KQkq", "KQRkq"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FenString(FenString.INITIAL_BOARD.replace("KQkq", "QQkk"))).isInstanceOf(IllegalArgumentException.class);
    }

    // Testing en passant target with invalid ranks
    @Test
    public void enPassantTargetInvalidRanks() {
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq b2")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq g7")).isInstanceOf(IllegalArgumentException.class);
    }

    // Testing move counters with invalid values
    @Test
    public void moveCountersInvalidValues() {
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - -1 1")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 -1")).isInstanceOf(NumberFormatException.class);
    }

    // Testing valid scenarios with minimum and maximum values for move counters
    @Test
    public void moveCountersValidExtremes() {
        FenString fsMin = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertThat(fsMin.getHalfmoveClock()).isEqualTo(0);
        assertThat(fsMin.getFullmoveCounter()).isEqualTo(1);

        // Assuming 999 is a valid maximum for the sake of this example
        FenString fsMax = new FenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 100 999");
        assertThat(fsMax.getHalfmoveClock()).isEqualTo(100);
        assertThat(fsMax.getFullmoveCounter()).isEqualTo(999);
    }
}
