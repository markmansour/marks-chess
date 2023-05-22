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

    public void validBishopMove() {
    }

    public void validBishopTake() {
    }

    public void validKingMove() {
    }

    public void validKingTake() {
    }

    public void validQueenMove() {
    }

    public void validQueenTake() {
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
    public void castling() {
    }

    public void promotion() {
    }
}
