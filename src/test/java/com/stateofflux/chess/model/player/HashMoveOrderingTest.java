package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The transposition table stores a best move per position; the search must try it first (hash-move
 * ordering) and must ignore a move that is not legal in the current position (which guards against
 * surfacing a garbage move after a key collision).
 */
@Tag("UnitTest")
public class HashMoveOrderingTest {

    private AlphaBetaPlayerWithTT player() {
        return new AlphaBetaPlayerWithTT(PlayerColor.WHITE, new PestoEvaluator());
    }

    @Test public void legalHashMoveIsTriedFirst() {
        Game game = new Game();
        MoveList<Move> moves = game.generateMoves();

        Move target = moves.get(5);                       // some move that is not already first
        // Reconstruct the move the way the TT would hand it back.
        Move hashMove = Move.buildFrom(target.toLong());

        player().orderHashMoveFirst(moves, hashMove);

        assertThat(moves.get(0).getFrom()).isEqualTo(target.getFrom());
        assertThat(moves.get(0).getTo()).isEqualTo(target.getTo());
        assertThat(moves).hasSize(20);                    // nothing added or dropped
    }

    @Test public void illegalHashMoveLeavesTheListUnchanged() {
        Game game = new Game();
        MoveList<Move> moves = game.generateMoves();
        List<Move> before = new ArrayList<>(moves);

        // a1a8 is not a legal move from the starting position (collision / foreign entry)
        Move foreign = new Move(Piece.WHITE_QUEEN, "a1", "a8", false);

        player().orderHashMoveFirst(moves, foreign);

        assertThat(moves).containsExactlyElementsOf(before);
    }
}
