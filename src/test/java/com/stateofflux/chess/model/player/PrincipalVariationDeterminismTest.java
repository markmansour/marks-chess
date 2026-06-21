package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tie-breaking among equal-scoring moves should happen only at the root (for variety). Inside the
 * tree it must be deterministic, otherwise the principal variation is not reproducible.
 */
@Tag("UnitTest")
public class PrincipalVariationDeterminismTest {

    private String pvOf(Game game) {
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(4);
        player.getNextMove(game);
        return player.getPrincipalVariation().stream().map(Move::toLongSan).collect(Collectors.joining(" "));
    }

    @Test public void principalVariationIsReproducible() {
        // Rxh4 is the single decisive move (K+R vs lone K), so the root choice is fixed. The rest
        // of the line has many equal-scoring replies; with interior randomness the PV tail varies
        // between runs.
        Game game = new Game("4k3/8/8/8/7q/8/8/4K2R w - - 0 1");

        String firstPv = pvOf(game);
        assertThat(firstPv).startsWith("h1h4");   // sanity: the forced winning move

        for (int i = 0; i < 12; i++)
            assertThat(pvOf(game)).isEqualTo(firstPv);
    }
}
