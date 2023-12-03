package com.stateofflux.chess.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.*;

public class GameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameTest.class);

    @BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
    }

    @Test
    public void scanInitialFenString() {
        Game g = new Game(FenString.INITIAL_BOARD);
        assertThat(g.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertThat(g.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(g.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(g.getEnPassantTarget()).isEqualTo("-");
        assertThat(g.getHalfmoveClock()).isZero();
        assertThat(g.getFullmoveCounter()).isOne();
    }

    @Test
    public void testDefaultGameSetup() {
        Game g = new Game();
        assertThat(g.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertThat(g.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(g.getCastlingRights().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(g.getEnPassantTarget()).isEqualTo("-");
        assertThat(g.getHalfmoveClock()).isZero();
        assertThat(g.getFullmoveCounter()).isOne();
    }

    @Test
    public void testNextMovesFromOpeningPosition() {
        Game game = new Game();
        game.generateMoves();
        MoveList<Move> gameMoves = game.getActivePlayerMoves();
        assertThat(gameMoves).hasSize(20);
        assertThat(gameMoves.asLongSan()).containsExactlyInAnyOrder("a2a3", "b2b3", "c2c3", "d2d3", "e2e3", "f2f3", "g2g3", "h2h3",
            "a2a4", "b2b4", "c2c4", "d2d4", "e2e4", "f2f4", "g2g4", "h2h4",
            "b1a3", "b1c3", "g1f3", "g1h3");
    }

    @Test
    public void validPawnMoveOneInitialSpace() {
        Game game = new Game();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        game.move("e3"); // from e2 to e3

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isOne();
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
        assertThat(game.getHalfmoveClock()).isOne();
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
        assertThat(game.getHalfmoveClock()).isOne();
        assertThat(game.getFullmoveCounter()).isOne();
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/ppp1pppp/8/3P4/8/8/PP1PPPPP/RNBQKBNR");
    }

    @Test
    public void validPawnMoveWithTwoTakeOptions() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P1P3/8/PP1P1PPP/RNBQKBNR b KQkq -");
        // black can move from d5 to c4 or e4
        game.generateMoves();
        MoveList<Move> potentialMoves = game.getActivePlayerMoves();
        assertThat(potentialMoves).hasSize(30);
        game.move("dxc4"); // from d5 to c4 - loctation 35 to 26

        assertThat(game.getCastlingRights()).isEqualTo("KQkq");
        assertThat(game.getEnPassantTarget()).isEqualTo("-");
        assertThat(game.getHalfmoveClock()).isOne();
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
        assertThat(game.getHalfmoveClock()).isOne();
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
        assertThat(game.getHalfmoveClock()).isOne();
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

    @Test public void takeKnightWithPawn() {
        Game game = new Game("rnbqkb1r/ppp2ppp/3p4/8/8/2n2N2/PPPP1PPP/R1BQKB1R w KQkq - 0 6");
        game.move("dxc3"); // move from d4 to c3 (but takes from d2)
        assertThat(game.asFen()).isEqualTo("rnbqkb1r/ppp2ppp/3p4/8/8/2P2N2/PPP2PPP/R1BQKB1R b KQkq - 1 6");
    }

    // https://www.chessprogramming.org/Castling
    public static class Castling {
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

            // are we generating the castling move as an option?
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();

            assertThat(generatedMoves.asLongSan()).contains("e1c1");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/2KR1BNR b kq -");
        }

        @Test
        public void kingSideWhiteCastling() {
            Game game = new Game("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQK2R w KQkq -");

            // are we generating the castling move as an option?
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).contains("e1g1");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQ1RK1 b kq -");
        }

        @Test
        public void queenSideBlackCastling() {
            Game game = new Game("r3kbnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR b KQkq -");

            // are we generating the castling move as an option?
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).contains("e8c8");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("2kr1bnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR w KQ -");
        }

        @Test
        public void kingSideBlackCastling() {
            Game game = new Game("rnbqk2r/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R b KQkq -");

            // are we generating the castling move as an option?
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).contains("e8g8");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbq1rk1/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R w KQ -");
        }
    }
    public static class WhenPassingThroughCheck {
        @Test
        public void kingDoesNotPassThroughCheckFromBishop() {
            Game game = new Game("rnbqk1nr/1pp3pp/5p2/p1bpp3/4P1PP/5P1N/PPPP2B1/RNBQK2R w KQkq -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n7/2pqP1B1/6b1/4P3/PP3PPP/RN1QKBNR b KQkq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKnight() {
            Game game = new Game("rnbqkb1r/pppppppp/8/8/6P1/5NnB/PPPPPP1P/RNBQK2R w KQkq -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p3pppp/bNnq4/2pP4/8/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromRook() {
            Game game = new Game("1nbqkbnr/1pp3Pp/4N3/8/4pr1P/p7/PPPP2B1/RNBQK2R w KQk -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/7q/3p2Q1/N1R1P3/PP3PPP/4KBNR b Kkq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromQueen() {
            Game game = new Game("rnb1kbnr/1pp3pp/5p2/p3p3/3qP1PP/7N/PPPP2B1/RNBQK2R w KQkq -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/2pq4/3p2Q1/4P3/PP3PPP/RN2KBNR b KQkq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKing() {
            Game game = new Game("rnB2b1r/ppp2p1p/5p2/8/8/8/P5kP/R1B1K2R w KQ -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3k1nr/p1K3pp/N7/5p2/2P1p2q/4PPP1/PP6/R1BQ1BNR b kq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test

        public void kingDoesNotPassThroughCheckFromPawn() {
            Game game = new Game("rn1k1b1r/ppp1pp1p/8/3N1b2/8/7P/P5p1/R1B1K2R w KQ -");
            game.generateMoves();
            MoveList<Move> generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p1P1pppp/b7/8/N1p3q1/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            game.generateMoves();
            generatedMoves = game.getActivePlayerMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }
    }

    public static class Promotion {
        @Test
        public void promotion() {
            fail("todo");
        }
    }

    @Test
    public void playGameWithValidMoves() {
        Game game = new Game();
        // 1. d4 Nf6 2. c4 e5 3. Nf3 c5 4. Nc3 cxd4 5. Nxd4 Bb4
        game.move("d4");
        game.move("Nf6");
        game.move("c4");
        game.move("e5");
        game.move("Nf3");
        game.move("c5");
        game.move("Nc3");
        game.move("cxd4");
        game.move("Nxd4");
        game.move("Bb4");

        assertThat(game.asFenNoCounters()).isEqualTo("rnbqk2r/pp1p1ppp/5n2/4p3/1bPN4/2N5/PP2PPPP/R1BQKB1R w KQkq -");
    }

    public static class InsufficientMaterialsTest {
        // Insufficient Materials
        @Test
        public void kingVsKing() {
            Game game = new Game("8/1K6/8/8/8/8/8/5k2 w - - 0 1"); // two kings
            assertThat(game.hasInsufficientMaterials()).isTrue();
        }

        @Test
        public void kingAndMinorPiecesVsKing() {
            Game game = new Game("8/8/8/8/8/8/1K3k1B/8 w - - 0 1");  // king & bishop vs king
            assertThat(game.hasInsufficientMaterials()).isTrue();

            game = new Game("3K4/8/8/8/8/8/1k2N3/8 w - - 0 1"); // king & knight vs king
            assertThat(game.isOver()).isTrue();
            assertThat(game.hasInsufficientMaterials()).isTrue();
        }

        @Test
        public void onlyKingVsAllOpponentPieces() {
            Game game = new Game("8/P2kP1BR/5Q2/6P1/NP2P3/P7/2PKPR2/1B2N3 w - - 0 1");  // black king vs all white
            assertThat(game.hasInsufficientMaterials()).isFalse();
            game.markTimeComplete();
            assertThat(game.hasInsufficientMaterials()).isTrue();
        }

        @Test
        public void kingAndTwoKnightsVsKing() {
            Game game = new Game("4N3/3K4/k7/8/8/8/8/5N2 w - - 0 1");  // black king vs white king and two white knights
            assertThat(game.hasInsufficientMaterials()).isTrue();
        }

        @Test
        public void kingAndMinorVsKingAndMinor() {
            String[] fens = new String[]{
                "1N6/8/8/3K2n1/8/8/3k4/8 w - - 0 1",  // black king & black knight vs white king & white knight
                "5k2/b7/8/8/8/4N3/6K1/8 w - - 0 1",   // black king & black bishop vs white king & white knight
                "8/8/4k3/8/8/8/4B2K/b7 w - - 0 1"     // black king & black bishop vs white king & white bishop
            };
            for (String fen : fens) {
                Game game = new Game(fen);
                assertThat(game.hasInsufficientMaterials()).isTrue();
            }
        }

        @Test public void fromFISCGamesDB() {
            String[] fens = new String[]{
                "8/7k/8/7K/8/8/8/8 w - -"  // black king & black bishop vs white king & white bishop
            };
            for (String fen : fens) {
                Game game = new Game(fen);
                assertThat(game.hasInsufficientMaterials()).isTrue();
            }
        }

    }

    public static class CheckTest {
        @Test public void nextMoveGetsOutOfCheck() {
            Game game = new Game("8/5k1p/1p2b1PP/4K3/1P6/P7/8/2q5 b - -");
            assertThat(game.isChecked(PlayerColor.BLACK)).isTrue();

            // generate next set of moves for the active player
            game.generateMoves();
            var moves = game.getActivePlayerMoves();

            // all moves take black out of check
            for(var move : moves) {
                Game newGame = new Game(game.asFen());
                LOGGER.info("fen: " + newGame.asFen());
                newGame.move(move);
                LOGGER.info("move: " + move);
                LOGGER.info("inCheck? " + newGame.isChecked(PlayerColor.BLACK));
                assertThat(newGame.isChecked(PlayerColor.BLACK)).isFalse();
                assertThat(game.isCheckmated(PlayerColor.BLACK)).isFalse();
            }
        }

        @Test public void isCheckmated() {
            Game game = new Game("6Q1/3rk3/4Q3/pp4P1/8/5P2/5PK1/8 b - -");
            assertThat(game.isChecked(PlayerColor.BLACK)).isTrue();
            assertThat(game.isCheckmated(PlayerColor.BLACK)).isTrue();
        }

        @Test public void cannotMoveIntoCheck() {
            Game game = new Game("r2q1k2/7n/p2N2nb/1ppp1b1p/1P5P/P1PP1P1B/3NP3/R1RK4 b - -");
            game.generateMoves();
            // LOGGER.info(game.getActivePlayerMoves().asLongSan().toString());
            assertThat(game.getActivePlayerMoves().asLongSan()).doesNotContain("f8e8");  // d6 knight can take king in e8
        }

        @Test public void fromFISCGamesDB() {
            String[] fens = new String[]{
                "rk5r/p1Q1R1pp/3p1p2/6B1/8/2P4P/1PP2PP1/3K4 b - -", // 530202352
                "4Q1kr/ppb3pp/2p5/2P3q1/8/8/PP3PPP/R5K1 b - -", // 530202201
                "5rk1/p4ppp/8/1QP5/8/8/PP3qrP/R2RK3 w - -", // 530201993
                "5k2/ppbb1Qp1/2p4p/8/2BP2Pq/8/PP3P2/1R3K2 b - -", // 530201946
                "7r/2k5/2p5/8/5bnK/8/4q3/8 w - -" // 530201658
            };
            for (String fen : fens) {
                Game game = new Game(fen);
                assertThat(game.isCheckmated(game.activePlayerColor)).isTrue();
            }
        }
    }

    @Test public void castlingRightsSetAfterMove() {
        Game game = new Game("r1b1nbnr/3k3p/Pp1pp1p1/2p5/P4q2/R1P5/1BNPPPBP/Q3K2R w K - 0 25");
        LOGGER.info(game.asFen());
        LOGGER.info("--------------------------");
        game.move("Kf1");  // move King from e1 to f1
        LOGGER.info(game.asFen());
        assertThat(game.getCastlingRights()).isEqualTo("-");
    }

    public static class Repetition {
        @Test public void getGameState() {
            Game g = new Game();
            long hash = g.getZobristKey();
            assertThat(hash).isEqualTo(4516155336704681926L);
        }

        @Test public void testThreefoldRepetition() {
            Game game = Game.fromSan("1. e4 e5 2. Be2 Be7 3. Bf1 Bf8 4. Bd3 Bd6 5. Bf1 Bf8 6. Bd3 Bd6 7. Bf1 Bf8");
            assertThat(game.isRepetition()).isTrue();
        }

        @Test
        public void testThreefoldRepetition1()  {
            Game game = Game.fromSan("1. e4 e5 2. Nf3 Nf6 3. Ng1 Ng8 4. Ke2 Ke7 5. Ke1 Ke8 6. Na3 Na6 7. Nb1 Nb8");
            assertThat(game.isRepetition()).isFalse();
        }

        @Test
        public void testThreefoldRepetition2() {
            // There is a repetition in the 50th move, but the 51st move overwrites the repetition
            Game game = Game.fromSan("1. Nf3 Nf6 2. c4 c5 3. b3 d6 4. d4 cxd4 5. Nxd4 e5 6. Nb5 Be6 7. g3 a6 8. N5c3 d5 9. cxd5 Nxd5 10. Bg2 Bb4 11. Bd2 Nc6 12. O-O O-O 13. Na4 Rc8 14. a3 Be7 15. e3 b5 16. Nb2 Qb6 17. Nd3 Rfd8 18. Qe2 Nf6 19. Nc1 e4 20. Bc3 Nd5 21. Bxe4 Nxc3 22. Nxc3 Na5 23. N1a2 Nxb3 24. Rad1 Bc4 25. Qf3 Qf6 26. Qg4 Be6 27. Qe2 Rxc3 28. Nxc3 Qxc3 29. Rxd8+ Bxd8 30. Rd1 Be7 31. Bb7 Nc5 32. Qf3 g6 33. Bd5 Bxd5 34. Qxd5 Qxa3 35. Qe5 Ne6 36. Ra1 Qd6 37. Qxd6 Bxd6 38. Rxa6 Bc5 39. Kf1 Kf8 40. Ke2 Ke7 41. Kd3 Kd7 42. g4 Kc7 43. Ra8 Kc6 44. f4 Be7 45. Rc8+ Kd5 46. Re8 Kd6 47. g5 f5 48. Rb8 Kc6 49. Re8 Kd6 50. Rb8 Kc6 51. Re8 Kd6");
            assertThat(game.isRepetition()).isFalse();
        }

        @Test
        public void testThreefoldRepetition3() {
            Game game = Game.fromSan("1. Nf3 Nf6 2. Nc3 c5 3. e3 d5 4. Be2 Ne4 5. Bf1 Nf6 6. Be2 Ne4 7. Bf1 Nf6");
            assertThat(game.isRepetition()).isTrue();
        }

        @Test
        public void testThreefoldRepetition4() {
            Game game = Game.fromSan("1. d4 d5 2. Nf3 Nf6 3. c4 e6 4. Bg5 Nbd7 5. e3 Be7 6. Nc3 O-O 7. Rc1 b6 8. cxd5 exd5 9. Qa4 c5 10. Qc6 Rb8 11. Nxd5 Bb7 12. Nxe7+ Qxe7 13. Qa4 Rbc8 14. Qa3 Qe6 15. Bxf6 Qxf6 16. Ba6 Bxf3 17. Bxc8 Rxc8 18. gxf3 Qxf3 19. Rg1 Re8 20. Qd3 g6 21. Kf1 Re4 22. Qd1 Qh3+ 23. Rg2 Nf6 24. Kg1 cxd4 25. Rc4 dxe3 26. Rxe4 Nxe4 27. Qd8+ Kg7 28. Qd4+ Nf6 29. fxe3 Qe6 30. Rf2 g5 31. h4 gxh4 32. Qxh4 Ng4 33. Qg5+ Kf8 34. Rf5 h5 35. Qd8+ Kg7 36. Qg5+ Kf8 37. Qd8+ Kg7 38. Qg5+ Kf8");
            assertThat(game.isRepetition()).isTrue();
        }

        @Test
        public void testThreefoldRepetition5()  {
            Game game = new Game("6k1/8/8/8/6p1/8/5PR1/6K1 w - - 0 32");
            // 1. f4 Kf7 2. Kf2 Kg8 3. Kg1 Kh7 4. Kh2 Kg8 5. Kg1
            game.move("f4");  // initial position - two square pawn advance
            game.move("Kf7"); // en passant capture not possible - would expose own king to check
            game.move("Kf2");
            game.move("Kg8");
            game.move("Kg1"); // twofold repetition
            game.move("Kh7");
            game.move("Kh2");
            game.getBoard().printOccupied();
            game.move("Kg8");
            game.getBoard().printOccupied();
            game.move("Kg1");
            assertThat(game.isRepetition()).isTrue();
        }

        @Test
        public void testThreefoldRepetition6() {
            Game game = new Game("8/8/8/8/4p3/8/R2P3k/K7 w - - 0 37");
            // 1. d4+ Kh3 2. Ra3+ Kh2 3. Ra2+ Kh1 4. Ra3 Kh2 5. Ra2+
            game.move("d4+");  // initial position - two square pawn advance
            game.move("Kh3");  // en passant capture not possible - would expose own king to check
            game.move("Ra3+");
            game.move("Kh2");
            game.move("Ra2+"); // twofold repetition
            game.move("Kh1");
            game.move("Ra3");
            game.move("Kh2");
            game.move("Ra2+"); // threefold repetiton

            assertThat(game.isRepetition()).isTrue();
        }

        // En Passent is possible instead of Bc4.  Therefore, it is a different position that later recurrences.
        @Test public void testThreefoldRepetition7()  {
            Game game = Game.fromSan("1. e4 Nf6 2. e5 d5 3. Bc4 Nc6 4. Bf1 Nb8 5. Bc4 Nc6 6. Bf1 Nb8");
            assertThat(game.isRepetition()).isFalse();
        }
    }

    @Test public void canPlayAFullGame() {
        Game game = new Game();
        game.disable50MovesRule();
        int counter = 0;
//        while(!game.isOver() && counter++ < 55) {
        while(!game.isOver() && counter++ < 1055) {
            LOGGER.info("--------------------------");
            game.generateMoves();
            var moves = game.getActivePlayerMoves();
            var move = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
            LOGGER.info("move: " + move);
            game.move(move);
            LOGGER.info("%d: %s check? %s --- %s check? %s === opponent checkmate? %s".formatted(
                counter,
                game.getActivePlayerColor(),
                game.isChecked(game.getActivePlayerColor()),
                game.getWaitingPlayer(),
                game.isChecked(game.getWaitingPlayer()),
                game.isCheckmated(game.getWaitingPlayer())));
            game.getBoard().printOccupied();
            LOGGER.info(game.asFen());
        }

        LOGGER.info("--------------------------");
        LOGGER.info("isOver: {}", game.isOver());
        LOGGER.info("isCheckmated: {}", game.isCheckmated(game.activePlayerColor));
        LOGGER.info("hasResigned: {}", game.hasResigned());
        LOGGER.info("isStalemate: {}", game.isStalemate());
        LOGGER.info("hasInsufficientMaterials: {}", game.hasInsufficientMaterials());
        LOGGER.info("exceededMoves: {}", game.exceededMoves());
        LOGGER.info("hasRepeated: {}", game.hasRepeated());
        assertThat(game.isOver()).isTrue();
    }

    public static class PgnGame {
        @Test public void playToCheckmate() {
            String pgnString = """
                [Event "FICS rated standard game"]
                [Site "FICS freechess.org"]
                [FICSGamesDBGameNo "530202352"]
                [White "Indrayoga"]
                [Black "scalaQueen"]
                [WhiteElo "2001"]
                [BlackElo "2051"]
                [WhiteRD "43.7"]
                [BlackRD "26.8"]
                [BlackIsComp "Yes"]
                [TimeControl "900+0"]
                [Date "2023.01.31"]
                [Time "18:23:00"]
                [WhiteClock "0:15:00.000"]
                [BlackClock "0:15:00.000"]
                [ECO "C42"]
                [PlyCount "41"]
                [Result "1-0"]
                                
                1. e4 e5 2. Nf3 Nf6 3. Nxe5 d6 4. Nf3 Nxe4 5. Nc3 Nxc3 6. dxc3 Bg4 7. h3 Bxf3 8. Qxf3 Qe7+ 9. Be3 Nc6 10. Bb5 Kd7 11. O-O-O Qe6 12. Rhe1 Qxa2 13. Bg5 f6 14. Qg4+ Kd8 15. Bxc6 bxc6 16. Qe4 Qa1+ 17. Kd2 Kc8 18. Qxc6 Qxd1+ 19. Kxd1 Be7 20. Rxe7 Kb8 21. Qxc7# {Black checkmated} 1-0
                """;

            Pgn pgn = new Pgn(pgnString);
            Game game = new Game(pgn);
            assertThat(game.isOver()).isTrue();
        }
    }
}
