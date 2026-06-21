package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The piece-square, castling, side-to-move and en-passant keys must each occupy a disjoint slice of
 * the random table. If two distinct atoms share a table slot they produce the same long, which makes
 * them XOR-cancel and lets distinct positions hash to the same key (corrupting repetition detection).
 */
@Tag("UnitTest")
class ZobristIndexRangeTest {
    @Test public void everyAtomicKeyIsDistinct() {
        ZobristHasher hasher = new ZobristHasher();
        Map<Long, String> seen = new HashMap<>();

        for (Piece piece : Piece.values()) {
            if (piece == Piece.EMPTY) continue;
            for (int square = 0; square < 64; square++)
                record(seen, hasher.getPieceSquareKey(piece, square), "piece " + piece + " @ " + square);
        }

        for (int castlingRights = 0; castlingRights < 16; castlingRights++)
            record(seen, hasher.getCastleRightsKey(castlingRights), "castling " + castlingRights);

        record(seen, hasher.getSideKey(PlayerColor.WHITE), "side WHITE");
        record(seen, hasher.getSideKey(PlayerColor.BLACK), "side BLACK");

        for (int epTarget = 0; epTarget < 64; epTarget++)
            record(seen, hasher.getEnPassantKey(epTarget), "en passant " + epTarget);
    }

    private void record(Map<Long, String> seen, long key, String description) {
        String previous = seen.put(key, description);
        assertThat(previous)
            .as("Zobrist key collision between [%s] and [%s]", previous, description)
            .isNull();
    }
}
