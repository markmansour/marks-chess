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
        assertThat(g.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(new String(g.getEnPassantTarget())).isEqualTo("-");
        assertThat(g.getHalfmoveClock()).isZero();
        assertThat(g.getFullmoveCounter()).isOne();
    }

    @Test
    public void testDefaultGameSetup() {
        Game g = new Game();
        assertThat(g.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertThat(g.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(g.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(new String(g.getEnPassantTarget())).isEqualTo("-");
        assertThat(g.getHalfmoveClock()).isZero();
        assertThat(g.getFullmoveCounter()).isOne();
    }

    @Test
    public void testNextMovesFromOpeningPosition() {
        Game game = new Game();
        int gameMovesCount = game.generateMoves();
        assertThat(game.getNextMoves()).hasSize(10);
        assertThat(gameMovesCount).isEqualTo(20);
    }

    @Test
    public void validPawnMoveOneInitialSpace() {
        Game game = new Game();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        game.move("e3"); // from e2 to e3

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR");
    }

    @Test
    public void validPawnMoveTwoInitialSpaces() {
        Game game = new Game();

        game.move("e4"); // from e2 to e4

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR");
    }

    @Test
    public void validPawnMoveTake() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -");
        game.move("cxd5"); // from c4 to d5

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/ppp1pppp/8/3P4/8/8/PP1PPPPP/RNBQKBNR");
    }

    @Test
    public void validPawnMoveWithTwoTakeOptions() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P1P3/8/PP1P1PPP/RNBQKBNR b KQkq -");
        // black can move from d5 to c4 or e4
        game.move("dxc4"); // from d5 to c4 - loctation 35 to 26

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/ppp1pppp/8/8/2p1P3/8/PP1P1PPP/RNBQKBNR");

        // second scenario
        game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P1P3/8/PP1P1PPP/RNBQKBNR b KQkq -");
        game.move("dxe4"); // from d5 to c4
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/ppp1pppp/8/8/2P1p3/8/PP1P1PPP/RNBQKBNR");
    }

    @Test
    public void validRookMove() {
        Game game = new Game("rnbqkbnr/1ppppppp/8/p7/P7/8/1PPPPPPP/RNBQKBNR w KQkq -");
        game.move("Ra3"); // from a1 to a3

        assertThat(game.getCastlingRights()).isEqualTo("Kkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/1ppppppp/8/p7/P7/R7/1PPPPPPP/1NBQKBNR");
    }

    @Test
    public void validRookTake() {
        Game game = new Game("r1bqkbnr/p1pppppp/n7/1P6/8/8/1PPPPPPP/RNBQKBNR w KQkq -");
        game.move("Rxa6"); // from a1 to a6

        assertThat(game.getCastlingRights()).isEqualTo("Kkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isZero();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("r1bqkbnr/p1pppppp/R7/1P6/8/8/1PPPPPPP/1NBQKBNR");
    }

    // NOTE TO ME: Implement all the tests below and the enPassant, castling and
    // promotion tests before refactoring!
    @Test
    public void validKnightMove() {
        Game game = new Game();
        game.move("Nc3"); // from b1 to c3
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/2N5/PPPPPPPP/R1BQKBNR b KQkq -");
    }

    @Test
    public void validKnightTake() {
        Game game = new Game("rnbqkbnr/p1pppppp/8/1p6/8/2N5/PPPPPPPP/R1BQKBNR w KQkq -");
        game.move("Nxb5"); // from c3 to b5
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/p1pppppp/8/1N6/8/8/PPPPPPPP/R1BQKBNR b KQkq -");
    }

    @Test
    public void validKnightTakeTwoOptions() {
        Game game = new Game("rnbqkb1r/pppppppp/8/3P4/8/5N2/PPPnPPPP/RNBQKB1R w KQkq -");
        game.move("Nfxd2"); // from f3 to d2, but could also be taken from b1.  because the target take of d2 is ambiguous, the file is needed
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkb1r/pppppppp/8/3P4/8/8/PPPNPPPP/RNBQKB1R b KQkq -");

        game = new Game("rnbqkb1r/pppppppp/8/3P4/8/5N2/PPPnPPPP/RNBQKB1R w KQkq -");
        game.move("Nbxd2"); // from b1 to d2
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkb1r/pppppppp/8/3P4/8/5N2/PPPNPPPP/R1BQKB1R b KQkq -");

        // two knights in the same file
        game = new Game("r2qkb1r/2p2p2/1p6/1N2p1p1/7P/2n2P1P/PPPPP3/RNBQKB1R w KQ -");
        game.move("N5xc3"); // from b5 to c3
        assertThat(game.asFenNoCounters()).isEqualTo("r2qkb1r/2p2p2/1p6/4p1p1/7P/2N2P1P/PPPPP3/RNBQKB1R b KQ -");

    }

    @Test
    public void validBishopMove() {
        Game game = new Game("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -");
        game.move("Bh6"); // from c1 to h6
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppp1ppp/7B/4p3/3P4/8/PPP1PPPP/RN1QKBNR b KQkq -");
    }

    @Test
    public void validBishopTake() {
        Game game = new Game("rnbqkbnr/pppppp1p/8/6p1/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -");
        game.move("Bxg5");
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppppp1p/8/6B1/3P4/8/PPP1PPPP/RN1QKBNR b KQkq -");
    }

    @Test
    public void validKingMove() {
        Game game = new Game("rnbqkbnr/pppppp1p/6p1/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq -");
        game.move("Kd2");
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppppp1p/6p1/8/3P4/8/PPPKPPPP/RNBQ1BNR b kq -");
    }

    @Test
    public void validKingTake() {
        Game game = new Game("rnbqkbnr/pppp1p1p/6p1/3P4/4p3/7P/PPPKPPP1/RNBQ1BNR b kq -");
        game.move("e3+");  // white king in check
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppp1p1p/6p1/3P4/8/4p2P/PPPKPPP1/RNBQ1BNR w kq -");
        game.move("Kxe3");
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pppp1p1p/6p1/3P4/8/4K2P/PPP1PPP1/RNBQ1BNR b kq -");
    }

    @Test
    public void validQueenMove() {
        Game game = new Game("rnbqkbnr/1ppppppp/8/p7/8/4P3/PPPP1PPP/RNBQKBNR w KQkq -");
        game.move("Qf3");
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/1ppppppp/8/p7/8/4PQ2/PPPP1PPP/RNB1KBNR b KQkq -");
    }

    @Test
    public void validQueenTake() {
        Game game = new Game("rnbqkbnr/1ppppp1p/8/p5p1/8/4PQ2/PPPP1PPP/RNB1KBNR w KQkq -");
        game.move("Qxf7+"); // black king in check
        assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/1ppppQ1p/8/p5p1/8/4P3/PPPP1PPP/RNB1KBNR b KQkq -");
    }

    // only test where the state of the board changes
    // https://www.chessprogramming.org/En_passant#bugs
    // http://www.talkchess.com/forum3/viewtopic.php?f=2&t=65218å
    // https://en.wikipedia.org/wiki/En_passant
    @Test
    public void enPassant() {
        // taken from https://www.chessprogramming.org/En_passant#bugs
        Game game = new Game("2r3k1/1q1nbppp/r3p3/3pP3/p1pP4/P1Q2N2/1PRN1PPP/2R4K w - - 0 22");
        game.move("b4"); // from b2 to b4 - creating an en passant situation

        assertThat(game.getCastlingRights()).isEqualTo("-");
        assertThat(game.getEnPassantTarget()).isEqualTo("b3");
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("2r3k1/1q1nbppp/r3p3/3pP3/pPpP4/P1Q2N2/2RN1PPP/2R4K");

        game = new Game("rnbqkbnr/ppppppp1/8/1P1P4/8/2P4p/P3PPPP/RNBQKBNR b KQkq -");
        game.move("c5"); // from c7 to c5 - creating an en passant situation

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("c6");
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4p/P3PPPP/RNBQKBNR");
    }

    // https://www.chessprogramming.org/Castling
    public class Castling {
        /*
         * TODO: finish writing the castling validation code.
         *
         * Current State:
         * I've implemented the castling move, but I haven't implemented validation.
         *
         * Next steps:
         * Implement the checks for the three prerequisites listed below in the generateMoves
         * method.
         */

        /*
        The prequisites for doing it are as follows:
        - the king and the relevant rook must not be moved, considered as castling rights inside a chess position
        - the king must not be in check
        - no square between king's start and final square may be controlled by the enemy
        */

        // basic castling
        @Test
        public void queenSideWhiteCastling() {
            Game game = new Game("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/R3KBNR w KQkq -");
            game.move("O-O-O");

            assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/2KR1BNR");
            assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
            assertThat(game.getCastlingRights()).isEqualTo("kq");
        }

        @Test
        public void kingSideWhiteCastling() {
            Game game = new Game("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQK2R w KQkq -");
            game.move("O-O");

            assertThat(game.getPiecePlacement()).isEqualTo("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQ1RK1");
            assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
            assertThat(game.getCastlingRights()).isEqualTo("kq");
        }

        @Test
        public void queenSideBlackCastling() {
            Game game = new Game("r3kbnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR b KQkq -");
            game.move("O-O-O");

            assertThat(game.getPiecePlacement()).isEqualTo("2kr1bnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR");
            assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
            assertThat(game.getCastlingRights()).isEqualTo("KQ");
        }

        @Test
        public void kingSideBlackCastling() {
            Game game = new Game("rnbqk2r/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R b KQkq -");
            game.move("O-O");

            assertThat(game.getPiecePlacement()).isEqualTo("rnbq1rk1/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R");
            assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
            assertThat(game.getCastlingRights()).isEqualTo("KQ");
        }
    }

    public class Promotion {
        @Test
        public void promotion() {
            fail("todo");
        }
    }
}
