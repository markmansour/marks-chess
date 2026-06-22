package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hash-move ordering can be toggled so its effect on the search can be measured A/B in one process.
 * Toggling it must not change the result of the search, only how many nodes it takes to get there.
 */
@Tag("UnitTest")
public class HashMoveOrderingToggleTest {

    private AlphaBetaPlayerWithTT player(boolean hashMoveOrdering) {
        AlphaBetaPlayerWithTT player = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
        player.setSearchDepth(4);
        player.setIncrement(TimeUnit.MINUTES.toNanos(5));   // effectively no timeout for a fixed-depth search
        player.setHashMoveOrdering(hashMoveOrdering);
        return player;
    }

    @Test public void searchResultIsUnchangedByHashMoveOrdering() {
        // Rxh4 is the single decisive move, so the root choice is fixed regardless of ordering.
        String fen = "4k3/8/8/8/7q/8/8/4K2R w - - 0 1";

        Move withOrdering = player(true).getNextMove(new Game(fen));
        Move withoutOrdering = player(false).getNextMove(new Game(fen));

        assertThat(withOrdering.toLongSan()).isEqualTo("h1h4");
        assertThat(withoutOrdering.toLongSan()).isEqualTo("h1h4");
    }

    @Test public void hashMoveOrderingIsEnabledByDefault() {
        assertThat(new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator()).isHashMoveOrdering()).isTrue();
    }
}
