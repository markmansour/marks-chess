package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.PawnMoves;
import com.stateofflux.chess.model.player.Evaluator;
import com.stateofflux.chess.model.player.Player;
import com.stateofflux.chess.model.player.RandomMovePlayer;
import com.stateofflux.chess.model.player.SimpleEvaluator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.*;

@Tag("UnitTest")
public class GameTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameTest.class);

    @Test
    public void scanInitialFenString() {
        Game g = new Game(FenString.INITIAL_BOARD);

        assertThat(g.getPiecePlacement()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertThat(g.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(g.board.getCastlingRightsForFen().toCharArray()).containsExactlyInAnyOrder('K', 'Q', 'k', 'q');
        assertThat(g.board.getEnPassantTargetAsFen()).isEqualTo("-");
        assertThat(g.getHalfMoveClock()).isZero();
        assertThat(g.getFullMoveCounter()).isOne();
    }

    @Test
    public void testDefaultGameSetup() {
        Game g = new Game();
        assertThat(g.asFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0");
    }

    @Test
    public void testNextMovesFromOpeningPosition() {
        Game game = new Game();
        MoveList<Move> gameMoves = game.generateMoves();
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
        assertThat(game.asFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR b KQkq - 1 0");
    }

    @Test
    public void validPawnMoveTwoInitialSpaces() {
        Game game = new Game();

        game.move("e4"); // from e2 to e4

        assertThat(game.asFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 1 0");
    }

    @Test
    public void validPawnMoveTake() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P5/8/PP1PPPPP/RNBQKBNR w KQkq -");
        game.move("cxd5"); // from c4 to d5

        assertThat(game.asFen()).isEqualTo("rnbqkbnr/ppp1pppp/8/3P4/8/8/PP1PPPPP/RNBQKBNR b KQkq - 1 1");
    }

    @Test
    public void validPawnMoveWithTwoTakeOptions() {
        Game game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P1P3/8/PP1P1PPP/RNBQKBNR b KQkq -");
        // black can move from d5 to c4 or e4
        MoveList<Move> gameMoves = game.generateMoves();

        assertThat(gameMoves).hasSize(30);
        game.move("dxc4"); // from d5 to c4 - loctation 35 to 26

        assertThat(game.asFen()).isEqualTo("rnbqkbnr/ppp1pppp/8/8/2p1P3/8/PP1P1PPP/RNBQKBNR w KQkq - 1 1");

        // second scenario
        game = new Game("rnbqkbnr/ppp1pppp/8/3p4/2P1P3/8/PP1P1PPP/RNBQKBNR b KQkq -");
        game.move("dxe4"); // from d5 to c4
        assertThat(game.asFen()).isEqualTo("rnbqkbnr/ppp1pppp/8/8/2P1p3/8/PP1P1PPP/RNBQKBNR w KQkq - 1 1");
    }

    @Test
    public void validRookMove() {
        Game game = new Game("rnbqkbnr/1ppppppp/8/p7/P7/8/1PPPPPPP/RNBQKBNR w KQkq -");
        game.move("Ra3"); // from a1 to a3

        assertThat(game.asFen()).isEqualTo("rnbqkbnr/1ppppppp/8/p7/P7/R7/1PPPPPPP/1NBQKBNR b Kkq - 1 1");
    }

    @Test
    public void validRookTake() {
        Game game = new Game("r1bqkbnr/p1pppppp/n7/1P6/8/8/1PPPPPPP/RNBQKBNR w KQkq -");
        game.move("Rxa6"); // from a1 to a6

        assertThat(game.asFen()).isEqualTo("r1bqkbnr/p1pppppp/R7/1P6/8/8/1PPPPPPP/1NBQKBNR b Kkq - 1 1");
    }

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
    public void twoKnightsOnTheSameFile() {
        // two knights in the same file
        Game game = new Game("rn1qkb1r/1p3ppp/p2pbn2/1N2p3/2P5/1P4P1/P3PP1P/RNBQKB1R w KQkq -");
        game.move("N5c3"); // from b5 to c3
        assertThat(game.asFenNoCounters()).isEqualTo("rn1qkb1r/1p3ppp/p2pbn2/4p3/2P5/1PN3P1/P3PP1P/RNBQKB1R b KQkq -");
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

        assertThat(game.board.getCastlingRights()).isEqualTo(0);
        assertThat(game.board.getEnPassantTargetAsFen()).isEqualTo("b3");
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.BLACK);
        // 2r3k1/1q1nbppp/r3p3/3pP3/pPpP4/P1Q2N2/2RN1PPP/2R4K b - - 1 22
        // 2r3k1/1q1nbppp/r3p3/3pP3/1P1P4/PpQ2N2/2RN1PPP/2R4K b - - 1 22
        assertThat(game.getPiecePlacement()).isEqualTo("2r3k1/1q1nbppp/r3p3/3pP3/pPpP4/P1Q2N2/2RN1PPP/2R4K");

        game = new Game("rnbqkbnr/ppppppp1/8/1P1P4/8/2P4p/P3PPPP/RNBQKBNR b KQkq -");
        game.move("c5"); // from c7 to c5 - creating an en passant situation

        assertThat(game.board.getCastlingRightsForFen()).isEqualTo("KQkq");
        assertThat(game.board.getEnPassantTargetAsFen()).isEqualTo("c6");
        assertThat(game.getActivePlayerColor()).isEqualTo(PlayerColor.WHITE);
        assertThat(game.getPiecePlacement()).isEqualTo("rnbqkbnr/pp1pppp1/8/1PpP4/8/2P4p/P3PPPP/RNBQKBNR");
    }

    @Test public void enPassantBlack() {
        Game game = new Game("rnbqkbnr/2pppppp/p7/Pp6/8/8/1PPPPPPP/RNBQKBNR w KQkq b6");
        assertThat(game.generateMoves().asLongSan()).containsOnly("a1a2", "a1a3", "a1a4", "a5b6", "b1a3", "b1c3", "b2b3", "b2b4", "c2c3", "c2c4", "d2d3", "d2d4", "e2e3", "e2e4", "f2f3", "f2f4", "g1f3", "g1h3", "g2g3", "g2g4", "h2h3", "h2h4");
    }

    @Test public void doubleEnPassant() {
        Game game = new Game("r1b1kbnr/ppp1pppp/2n1q3/3pP1P1/3P4/8/PPP2P1P/RNBQKBNR b KQkq -");
        game.generateMoves();
        // move p : f7f5 was crashing - it would put the white king in check.
        game.move("f5");

        assertThat(game.getBoard().getEnPassantTarget()).isEqualTo(45);
    }

    @Test public void enPassantTakeRemovesCheck() {
        Game game = new Game("8/5R2/4p3/1p4kp/1P4p1/8/P1Q2PKP/1r6 w - -");
        game.moveLongNotation("h2h4");  // game.move("h4+"); gives the same result.
        assertThat(game.asFen()).startsWith("8/5R2/4p3/1p4kp/1P4pP/8/P1Q2PK1/1r6 b - h3");
    }

    @Test public void enPassantBug() {
        // position startpos moves e2e4 e7e5 f2f4 e5f4 d1g4 g7g5 h2h3 d7d5 g4e2 f8g7 e4d5 g8e7 b1c3 b8d7 c3e4 e7f5 e2f3 d8e7 f1b5 f5d6 b5d3 h7h5 g1e2 e7e5
        String[] longFormMoves = "e2e4 e7e5 f2f4 e5f4 d1g4 g7g5 h2h3 d7d5 g4e2 f8g7 e4d5 g8e7 b1c3 b8d7 c3e4 e7f5 e2f3 d8e7 f1b5 f5d6 b5d3 h7h5 g1e2 e7e5".split(" ");
        Game game = new Game();
        for(String move : longFormMoves)
            game.moveLongNotation(move);

        String expectedFen = "r1b1k2r/pppn1pb1/3n4/3Pq1pp/4Np2/3B1Q1P/PPPPN1P1/R1B1K2R w KQkq -";
        assertThat(game.asFen()).startsWith(expectedFen);

        List<Move> moves = game.generateMoves();
        assertThat(moves.stream().map(Move::toLongSan).toList()).doesNotContain("d5e6");
    }

    @Test public void initialTwoSquareOpeningNextToPawnButNoEnPassant() {
        Game game = new Game("6k1/8/8/8/6p1/8/5PR1/6K1 w - - 0 32");
        // 1. f4 Kf7 2. Kf2 Kg8 3. Kg1 Kh7 4. Kh2 Kg8 5. Kg1
        game.move("f4");  // initial position - two square pawn advance
        // move from g4 to f3 would be possible IF it didn't put the black king in check.
        // therefore this is NOT an en passant move.
        assertThat(game.board.getEnPassantTarget()).isEqualTo(PawnMoves.NO_EN_PASSANT_VALUE);
        assertThat(game.generateMoves().asLongSan()).doesNotContain("g4f3");
    }

    @Test public void takeKnightWithPawn() {
        Game game = new Game("rnbqkb1r/ppp2ppp/3p4/8/8/2n2N2/PPPP1PPP/R1BQKB1R w KQkq - 0 6");
        game.move("dxc3"); // move from d4 to c3 (but takes from d2)
        assertThat(game.asFen()).isEqualTo("rnbqkb1r/ppp2ppp/3p4/8/8/2P2N2/PPP2PPP/R1BQKB1R b KQkq - 1 6");
    }

    @Nested
    class Ordering {
        @Test public void capturesBeforeNonCapture() {
            Game game = new Game("r1b1kbnr/ppp1pppp/2n1q3/3pP1P1/3P4/8/PPP2P1P/RNBQKBNR b KQkq -");
            game.generateMoves();
            // move p : f7f5 was crashing - it would put the white king in check.
            game.move("f5");
            List<Move> moves = game.generateMoves();
            assertThat(moves.get(0).toLongSan()).isNotEqualTo("g5f6");
            moves.sort(new CaptureOnlyMoveComparator());
            assertThat(moves.get(0).toLongSan()).isEqualTo("g5f6");
        }
    }

    // https://www.chessprogramming.org/Castling
    @Nested
    class Castling {
        /*
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
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).contains("e1c1");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqkbnr/pp2p2p/2p2pp1/3p4/3P1B2/2N5/PPPQPPPP/2KR1BNR b kq -");
        }

        @Test
        public void kingSideWhiteCastling() {
            Game game = new Game("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQK2R w KQkq -");

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).contains("e1g1");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbqk1nr/p1pp1ppp/8/1p2p3/1b4P1/5N1B/PPPPPP1P/RNBQ1RK1 b kq -");
        }

        @Test
        public void queenSideBlackCastling() {
            Game game = new Game("r3kbnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR b KQkq -");

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).contains("e8c8");

            // can we make the castling move?
            game.move("O-O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("2kr1bnr/ppp1pppp/2nq4/3p4/QPP3b1/3P4/P3PP1P/RNB1KBNR w KQ -");
        }

        @Test
        public void kingSideBlackCastling() {
            Game game = new Game("rnbqk2r/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R b KQkq -");

            // are we generating the castling move as an option?
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).contains("e8g8");

            // can we make the castling move?
            game.move("O-O");
            assertThat(game.asFenNoCounters()).isEqualTo("rnbq1rk1/pppppp1p/5npb/8/5P2/3PB2N/PPP1P1PP/RN1QKB1R w KQ -");
        }

        @Test public void queenSideBlackCastlingWhenBlocked() {
            Game game = new Game("rn2kbnr/ppp1pppp/3q4/3p4/8/P6N/RPPPPPPP/1NBQKB1R w Kkq -");
            MoveList<Move> moves = game.generateMoves();
            assertThat(moves.asLongSan()).doesNotContain("e1c8");  // white king to black rook with blockers
            assertThat(moves).hasSize(19);
        }

        @Test void hasBug() {
            Game game = new Game("rn2kbnr/p3p2p/1p3pp1/8/7P/3p4/PP3PQ1/RNB1K1N1 b Qkq - 1 11");
            assertThat(game.generateMoves().stream().map(Move::toLongSan).toList()).doesNotContain("e8c8");
        }

        @Test void rookBeingTakenRemovesCastlingRights() {
            Game game = new Game("N2k2nr/p2pnp1p/1p1p4/4b1p1/2P5/5B2/P4PbP/1R1QK1NR b K -");
            game.moveLongNotation("g2h1");
            assertThat(game.getBoard().getCastlingRights()).isEqualTo(0);
        }

    }

    @Test public void removeIllegalMoves() {
        Game game = new Game("r3kbnr/pp3ppp/n7/2pqP1B1/6b1/4P3/PP3PPP/RN1QKBNR b KQkq -");
        MoveList<Move> generatedMoves;

        generatedMoves = game.pseudoLegalMoves();
        assertThat(generatedMoves).hasSize(47);
        assertThat(generatedMoves.asLongSan()).contains("e8e7");  // move into check
        generatedMoves = game.generateMoves();
        assertThat(generatedMoves).hasSize(45);
        assertThat(generatedMoves.asLongSan()).doesNotContain("e8e7");
    }

    @Nested
    class WhenPassingThroughCheck {
        @Test
        public void kingDoesNotPassThroughCheckFromBishop() {
            Game game = new Game("rnbqk1nr/1pp3pp/5p2/p1bpp3/4P1PP/5P1N/PPPP2B1/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n7/2pqP1B1/6b1/4P3/PP3PPP/RN1QKBNR b KQkq -");
            generatedMoves = game.pseudoLegalMoves();
            assertThat(generatedMoves).hasSize(47);
            assertThat(generatedMoves.asLongSan()).contains("e8e7");  // move into check
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves).hasSize(45);
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8e7");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKnight() {
            Game game = new Game("rnbqkb1r/pppppppp/8/8/6P1/5NnB/PPPPPP1P/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p3pppp/bNnq4/2pP4/8/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromRook() {
            Game game = new Game("1nbqkbnr/1pp3Pp/4N3/8/4pr1P/p7/PPPP2B1/RNBQK2R w KQk -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/7q/3p2Q1/N1R1P3/PP3PPP/4KBNR b Kkq -");
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromQueen() {
            Game game = new Game("rnb1kbnr/1pp3pp/5p2/p3p3/3qP1PP/7N/PPPP2B1/RNBQK2R w KQkq -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/pp3ppp/n6B/2pq4/3p2Q1/4P3/PP3PPP/RN2KBNR b KQkq -");
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test
        public void kingDoesNotPassThroughCheckFromKing() {
            Game game = new Game("rnB2b1r/ppp2p1p/5p2/8/8/8/P5kP/R1B1K2R w KQ -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3k1nr/p1K3pp/N7/5p2/2P1p2q/4PPP1/PP6/R1BQ1BNR b kq -");
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }

        @Test

        public void kingDoesNotPassThroughCheckFromPawn() {
            Game game = new Game("rn1k1b1r/ppp1pp1p/8/3N1b2/8/7P/P5p1/R1B1K2R w KQ -");
            MoveList<Move> generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e1g1");

            game = new Game("r3kbnr/p1P1pppp/b7/8/N1p3q1/4P3/PP1P1PPP/R1BQKBNR b KQkq -");
            generatedMoves = game.generateMoves();
            assertThat(generatedMoves.asLongSan()).doesNotContain("e8c8");
        }
    }

    @Nested
    class Promotion {
        @Test
        public void basicPromotion() {
            Game game = new Game("8/Pk6/8/8/8/8/6Kp/8 w - - 0 1");
            game.move("a8=Q+");

            // draws
            assertThat(game.asFen()).isEqualTo("Q7/1k6/8/8/8/8/6Kp/8 b - - 1 1");
        }

        @Test public void promotionTakingQueen () {
            Game game = new Game("1q6/P6k/8/5N1K/8/8/8/8 w - -");
            assertThat(game.generateMoves().asLongSan()).contains("a7a8N", "a7a8B", "a7a8R", "a7a8Q");
            game.move("axb8=Q");
            assertThat(game.asFen()).isEqualTo("1Q6/7k/8/5N1K/8/8/8/8 b - - 1 1");
        }

        @Test void blackPawnPromotion() {
            Game game = new Game("rnbqkbnr/1ppppppp/8/8/8/8/p1PPPPPP/2BQKBNR b Kkq - 0 1");
            game.move("a1=b");  // promote to bishop
            assertThat(game.asFen()).isEqualTo("rnbqkbnr/1ppppppp/8/8/8/8/2PPPPPP/b1BQKBNR w Kkq - 1 1");
        }

        @Test void promotionWithIncorrectReplacement() {
            Game game = new Game("rnbqkbnr/1ppppppp/8/8/8/8/p1PPPPPP/2BQKBNR b Kkq - 0 1");
            game.move("a1=B");  // promote to white bishop, which is the wrong color.
            assertThat(game.asFen()).isEqualTo("rnbqkbnr/1ppppppp/8/8/8/8/2PPPPPP/b1BQKBNR w Kkq - 1 1");
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

        assertThat(game.asFen()).isEqualTo("rnbqk2r/pp1p1ppp/5n2/4p3/1bPN4/2N5/PP2PPPP/R1BQKB1R w KQkq - 0 5");
    }

    @Nested
    class InsufficientMaterialsTest {
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
                "8/7k/8/7K/8/8/8/8 w - -"  // two kings - whites turn
            };
            for (String fen : fens) {
                Game game = new Game(fen);
                assertThat(game.hasInsufficientMaterials()).isTrue();
            }
        }

        @Test public void fromFISCGamesDBReversed() {
            String[] fens = new String[]{
                "8/7k/8/7K/8/8/8/8 b - -"  // two kings - blacks turn
            };
            for (String fen : fens) {
                Game game = new Game(fen);
                assertThat(game.hasInsufficientMaterials()).isTrue();
            }
        }

        @Test public void kingCantTakeKingAndCantMove() {
            Game g = new Game("K7/2b5/7p/1k5P/8/8/8/8 b - -");
            g.move("Kb6");
            assertThat(g.isStalemate()).isTrue();
            assertThat(g.isDraw()).isTrue();
        }
    }

    @Nested
    class CheckTest {
        @Test public void nextMoveGetsOutOfCheck() {
            Game game = new Game("8/5k1p/1p2b1PP/4K3/1P6/P7/8/2q5 b - -");
            assertThat(game.isChecked()).isTrue();

            // generate next set of moves for the active player
            MoveList<Move> moves = game.generateMoves();

            // all moves take black out of check
            for(var move : moves) {
                Game newGame = new Game(game.asFen());
                newGame.move(move);
                assertThat(newGame.isChecked()).isFalse();
                assertThat(game.isCheckmated()).isFalse();
            }
        }

        @Test public void isCheckmated() {
            Game game = new Game("6Q1/3rk3/4Q3/pp4P1/8/5P2/5PK1/8 b - -");
            assertThat(game.isChecked()).isTrue();
            assertThat(game.isCheckmated()).isTrue();
        }

        @Test public void cannotMoveIntoCheck() {
            Game game = new Game("r2q1k2/7n/p2N2nb/1ppp1b1p/1P5P/P1PP1P1B/3NP3/R1RK4 b - -");
            MoveList<Move> moves = game.generateMoves();
            // LOGGER.info(game.getActivePlayerMoves().asLongSan().toString());
            assertThat(moves.asLongSan()).doesNotContain("f8e8");  // d6 knight can take king in e8
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
                assertThat(game.isCheckmated()).isTrue();
            }
        }
    }

    @Nested
    class ZobristKey {
        @Test public void keysAreRestoredAfterUndo() {
            Game game = new Game();
            MoveList<Move> moves = game.generateMoves();
            long hash = game.getZobristKey();
            for(var move : moves) {
                game.move(move);
                game.undo();
                assertThat(game.getZobristKey()).isEqualTo(hash);
            }
        }
    }
    @Nested
    class Repetition {
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
            // This is not three-fold repetition.  Move 47 (g5) creates an en passant move.  Moves 49 and 51 have
            // the same pattern as 47, but due to the en passant are only repeated twice.
            // according to https://support.chess.com/article/1042-i-got-a-draw-by-repetition-how-did-that-happen
            // then a draw occurs as soon as there is third repetition
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
            // This test make sure that pawns that are unable to en passant are marked as en passant when
            // generating the zobrist key.
            // So, for our first move, f4, although it looks like en passant can be performed, it cannot because it
            // would place the black king in check.
            Game game = new Game("6k1/8/8/8/6p1/8/5PR1/6K1 w - - 0 32");
            // 1. f4 Kf7 2. Kf2 Kg8 3. Kg1 Kh7 4. Kh2 Kg8 5. Kg1
            game.move("f4");  // HASH_1 - initial position - two square pawn advance
            game.move("Kf7"); // move 2: (initial) en passant capture not possible - would expose own king to check
            game.move("Kf2");
            game.move("Kg8");
            game.move("Kg1"); // HASH_1 - move 5: twofold repetition
            game.move("Kh7");
            game.move("Kh2");
            game.move("Kg8");
            game.move("Kg1"); // HASH_1 - move 9: threefold repetition
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

    @Disabled("Playing a full game with Random moves")
    @Test public void twoPlayersWithRandomMoves() {
        Evaluator evaluator = new SimpleEvaluator();
        Player randomPlayerOne = new RandomMovePlayer(PlayerColor.WHITE, evaluator);
        Player randomPlayerTwo = new RandomMovePlayer(PlayerColor.BLACK, evaluator);
        Game game = new Game();
        game.play(randomPlayerOne, randomPlayerTwo);

        game.printOccupied();
        assertTrue(game.isOver());
    }

    @Nested
    class PgnGame {
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
