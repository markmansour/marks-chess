package com.stateofflux.chess.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class ZobristHasherTest {
    @Test public void testFenHash() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -");
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
    }

    @Test public void testDefaultHash() {
        Game game = new Game();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
    }

    @Test public void move() {
        Game game = new Game();
        long originalKey = game.getZobristKey();
        game.calculateFullZorbistKey();
        game.move("a4");
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        assertThat(game.getZobristKey()).isEqualTo(originalKey);
    }

    @Test public void takePiece() {
//        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -");
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P5/8/PP1PPPPP/RNBQKBNR w - -");
        long originalKey = game.getZobristKey();
        game.move("cxd5"); // from c4 to d5

        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey()).isEqualTo(originalKey);
    }

    @Test public void enPassantSetUp() {
//        Game game = new Game("rnbqkbnr/ppppppp1/8/1P1P4/8/2P4p/P3PPPP/RNBQKBNR b KQkq -");
        Game game = new Game("rnbqkbnr/ppppppp1/8/1P1P4/8/2P4p/P3PPPP/RNBQKBNR b - -");
        long originalKey = game.getZobristKey();
        game.move("c5"); // c7d5 - create enpassant
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey()).isEqualTo(originalKey);
    }

    @Test public void enPassantAction() {
        // Game game = new Game("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4p/P3PPPP/RNBQKBNR w KQkq c6");
        Game game = new Game("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4p/P3PPPP/RNBQKBNR w - c6");
        long originalKey = game.getZobristKey();
        game.move("bxc6"); // c7d5 - action enpassant b5b6
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey()).isEqualTo(originalKey);
    }

    @Test public void promotePiece() {
        Game game = new Game("8/Pk6/8/8/8/8/6Kp/8 w - - 0 1");
        long originalKey = game.getZobristKey();
        game.move("a8=Q+");

        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey()).isEqualTo(originalKey);
    }

    @Test public void castling() {
        Game game = new Game("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/R3KBNR w KQkq -");

        long originalKey = game.getZobristKey();
        game.move("O-O-O");

        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey());
        game.undo();
        assertThat(game.getZobristKey()).isEqualTo(game.calculateFullZorbistKey()).isEqualTo(originalKey);
    }

}