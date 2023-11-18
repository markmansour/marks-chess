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
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
        Game g = new Game(fen);
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
        MoveList<Move> gameMoves = game.moves();
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
        MoveList<Move> potentialMoves = game.moves();
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

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).contains("e1c1");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/2KR1BNR b kq -");
        }

        @Test
        public void kingSideWhiteCastling() {
            Game game = new Game("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQK2R w KQkq -");

            // are we generating the castling move as an option?
            game.moves();
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).contains("e1g1");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQ1RK1 b kq -");
        }

        @Test
        public void queenSideBlackCastling() {
            Game game = new Game("r3kbnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR b KQkq -");

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).contains("e8c8");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("2kr1bnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR w KQ -");
        }

        @Test
        public void kingSideBlackCastling() {
            Game game = new Game("rnbqk2r/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R b KQkq -");

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).contains("e8g8");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbq1rk1/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R w KQ -");
        }
    }
    public class WhenPassingThroughCheck {
        @Test
        public void kingDoesNotPassThroughCheckFromBishop() {
            Game game = new Game("rnbqk1nr/1pp3pp/5p2/p1bpp3/4P1PP/5P1N/PPPP2B1/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n7/2pqP1B1/6b1/4P3/PP3PPP/RN1QKBNR b KQkq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKnight() {
            Game game = new Game("rnbqkb1r/pppppppp/8/8/6P1/5NnB/PPPPPP1P/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p3pppp/bNnq4/2pP4/8/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromRook() {
            Game game = new Game("1nbqkbnr/1pp3Pp/4N3/8/4pr1P/p7/PPPP2B1/RNBQK2R w KQk -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/7q/3p2Q1/N1R1P3/PP3PPP/4KBNR b Kkq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromQueen() {
            Game game = new Game("rnb1kbnr/1pp3pp/5p2/p3p3/3qP1PP/7N/PPPP2B1/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/2pq4/3p2Q1/4P3/PP3PPP/RN2KBNR b KQkq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKing() {
            Game game = new Game("rnB2b1r/ppp2p1p/5p2/8/8/8/P5kP/R1B1K2R w KQ -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3k1nr/p1K3pp/N7/5p2/2P1p2q/4PPP1/PP6/R1BQ1BNR b kq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test

        public void kingDoesNotPassThroughCheckFromPawn() {
            Game game = new Game("rn1k1b1r/ppp1pp1p/8/3N1b2/8/7P/P5p1/R1B1K2R w KQ -");
            MoveList<Move> generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p1P1pppp/b7/8/N1p3q1/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            generatedMoves = game.moves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }
    }

    public class Promotion {
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

    // Insufficient Materials
    @Test public void kingVsKing() {
        Game game = new Game("8/1K6/8/8/8/8/8/5k2 w - - 0 1"); // two kings
        assertThat(game.hasInsufficientMaterials()).isTrue();
    }

    @Test public void kingAndMinorPiecesVsKing() {
        Game game = new Game("8/8/8/8/8/8/1K3k1B/8 w - - 0 1");  // king & bishop vs king
        assertThat(game.hasInsufficientMaterials()).isTrue();

        game = new Game("3K4/8/8/8/8/8/1k2N3/8 w - - 0 1"); // king & knight vs king
        assertThat(game.isOver()).isTrue();
        assertThat(game.hasInsufficientMaterials()).isTrue();
    }

    @Test public void onlyKingVsAllOpponentPieces() {
        Game game = new Game("8/P2kP1BR/5Q2/6P1/NP2P3/P7/2PKPR2/1B2N3 w - - 0 1");  // black king vs all white
        assertThat(game.hasInsufficientMaterials()).isFalse();
        game.markTimeComplete();
        assertThat(game.hasInsufficientMaterials()).isTrue();
    }

    @Test public void kingAndTwoKnightsVsKing() {
        Game game = new Game("4N3/3K4/k7/8/8/8/8/5N2 w - - 0 1");  // black king vs white king and two white knights
        assertThat(game.hasInsufficientMaterials()).isTrue();
    }

    @Test public void kingAndMinorVsKingAndMinor() {
        String[] fens = new String[] {
            "1N6/8/8/3K2n1/8/8/3k4/8 w - - 0 1",  // black king & black knight vs white king & white knight
            "5k2/b7/8/8/8/4N3/6K1/8 w - - 0 1",   // black king & black bishop vs white king & white knight
            "8/8/4k3/8/8/8/4B2K/b7 w - - 0 1"     // black king & black bishop vs white king & white bishop
        };
        for (String fen : fens) {
            Game game = new Game(fen);
            assertThat(game.hasInsufficientMaterials()).isTrue();
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


    @Test public void canPlayAFullGame() {
        Game game = new Game();
        game.disable50MovesRule();
        int counter = 0;
//        while(!game.isOver() && counter++ < 55) {
        while(!game.isOver() && counter++ < 1055) {
            LOGGER.info("--------------------------");
            var moves = game.moves();
            var move = moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
            LOGGER.info("move: " + move);
            game.move(move);
            LOGGER.info("%s check? %s --- %s check? %s === checkmate? %s".formatted(
                game.getActivePlayerColor(),
                game.isCheck(game.getActivePlayerColor()),
                game.getWaitingPlayer(),
                game.isCheck(game.getWaitingPlayer()),
                game.isCheckmate()));
            game.getBoard().printOccupied();
            LOGGER.info(game.asFen());
        }

        LOGGER.info("--------------------------");
        assertThat(game.isOver()).isTrue();
    }
}
