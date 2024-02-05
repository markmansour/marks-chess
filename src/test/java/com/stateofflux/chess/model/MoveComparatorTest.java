package com.stateofflux.chess.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class MoveComparatorTest {

    @Test void noCaptures() {
        Game game = new Game();  // starting position
        MoveList<Move> moves = game.generateMoves();
        assertThat(moves).filteredOn(x -> x.isCapture()).hasSize(0);
    }

    @Test void oneCaptureOfEachType() {
        Game game = new Game("3k4/1n4q1/P1b2P2/1P2r3/3P3p/6P1/1P2P2P/RNBQKBNR w KQ - 0 1");
        MoveList<Move> moves = game.generateMoves();
        assertThat(moves).filteredOn(x -> x.isCapture()).hasSize(5);
        moves.sort(new CaptureOnlyMoveComparator());
        moves.subList(5, moves.size()).clear();
        assertThat(moves.stream().map(x -> x.toLongSan()).toList()).containsOnly(
            "f6g7",  // queen
            "d4e5", // rook
            "b5c6", // bishop
            "a6b7", // knight
            "g3h4" // pawn
        );

        moves.sort(new MvvLvaMoveComparator());
        assertThat(moves.stream().map(x -> x.toLongSan()).toList()).containsExactly(
            "f6g7",  // queen
            "d4e5", // rook
            "b5c6", // bishop
            "a6b7", // knight
            "g3h4" // pawn
        );
    }

    @Test void multipleCapturesOfEachType() {
        Game game = new Game("4kbnr/pp1ppppp/8/1n1b1r1q/P1P1P1P1/1p6/P5PP/RNBQKBNR w KQk - 0 1");
        MoveList<Move> moves = game.generateMoves();
        assertThat(moves).filteredOn(x -> x.isCapture()).hasSize(10);
        moves.sort(new MvvLvaMoveComparator());
        moves.subList(10, moves.size()).clear();

        assertThat(moves.get(0).toLongSan()).isEqualTo("g4h5"); // take queen by pawn
        assertThat(moves.subList(1,3).stream().map(x->x.toLongSan()).toList()).contains(
            "g4f5", // take rook by pawn
            "e4f5"  // take rook by pawn
        );
        assertThat(moves.subList(3,5).stream().map(x->x.toLongSan()).toList()).contains(
            "e4d5", // take bishop by pawn
            "c4d5"  // take bishop by pawn
        );
        assertThat(moves.get(5).toLongSan()).isEqualTo("d1d5"); // take bishop by queen
        assertThat(moves.subList(6,8).stream().map(x->x.toLongSan()).toList()).contains(
            "c4b5", // take knight by pawn
            "a4b5"  // take knight by pawn
        );
        assertThat(moves.get(8).toLongSan()).isEqualTo("a2b3"); // take pawn by pawn
        assertThat(moves.get(9).toLongSan()).isEqualTo("d1b3"); // take pawn by queen
    }

    @Test void twoMostValuableVictimsOrderByLeastValuableAggressor() {
        Game game = new Game("3k4/8/2r2r2/2Q1P3/8/8/1P1PP2P/RNB1KBNR w KQ - 0 1");
        MoveList<Move> moves = game.generateMoves();
        assertThat(moves).filteredOn(x -> x.isCapture()).hasSize(2);
        moves.sort(new MvvLvaMoveComparator());
        moves.subList(2, moves.size()).clear();
        assertThat(moves.stream().map(x -> x.toLongSan()).toList()).containsExactly(
            "e5f6", // pawn to rook - same victim strength, but pawn is least valuable aggressor
            "c5c6"  // queen to rook
        );
    }

}