package com.stateofflux.chess.model;

import com.stateofflux.chess.model.pieces.Piece;

public class ZobristHasher {
    /*
     * https://web.archive.org/web/20070810003508/www.seanet.com/%7Ebrucemo/topics/zobrist.htm
        # A means of enabling position comparison

        A chess position is comprised of the pieces on the board, side to move, castling legality, and en-passant
        capture legality.

        When writing a chess program, it is necessary to be able to compare two positions in order to see if they are
        the same.  If you had to compare the position of each piece, it wouldn't take long to do, but in practice you
        will have to do this thousands of times per second, so if you did it this way it could become a performance
        bottleneck.  Also, the number of positions that you will save for future comparison is extremely large, so
        storing the location of each piece, etc., ends up taking too much space.

        A solution to this problem involves creation of a signature value, typically 64-bits.  Since 64 bits is not
        enough to enumerate every chess position, there will exist the possibility of a signature collision, but in
        practice this is rare enough that it can be ignored, as long as you make sure you don't actually crash if you
        get a collision.

        Whether or not 32-bits is enough is open to some debate, but conventional wisdom says "no".

        A popular way of implementing signatures involves Zobrist keys.

        # Implementation

        You start by making a multi-dimensional array of 64-bit elements, each of which contains a random number.  In
        C, the "rand()" function returns a 15-bit value (0..32767), so in order to get a 64-bit random number you will
        probably have to do a little work.  Actually, I'll do it for you.  Here is a 64-bit random number function:

        U64 rand64(void)
        {
            return rand() ^ ((U64)rand() << 15) ^ ((U64)rand() << 30) ^
                ((U64)rand() << 45) ^ ((U64)rand() << 60);
        }

        This function works by filling up a whole "U64" element (you define this element yourself, depending upon what
        compiler you use -- try "long long" or "__int64") with hunks of gibberish returned by "rand()".

        Anyway, your table has three dimensions -- type of piece, color of piece, and location of piece:

        U64 zobrist[pcMAX][coMAX][sqMAX];
        You start by filling this up with random numbers when the program boots.  The numbers need to be pretty random.
        I've read Usenet posts and so on where people claim that "rand()" is not random enough for this, but "rand()"
        is what I've always used and I've never had a problem.  If you want to make something more random, more power
        to you, but make sure that you don't use anything less random than "rand()".

        To create a zobrist key for a position, you set your key to zero, then you go find every piece on the board,
        and XOR (via the "^" operator) "zobrist[pc][co][sq]" into your key.

        If the position is white to move, you leave it alone, but if it's black to move, you XOR a 64-bit constant
        random number into the key.

        # Why Zobrist keys are especially nice

        This Zobrist key technique creates keys that aren't related to the position being keyed.  If a single piece or
        pawn is moved, you get a key that's completely different, so these keys don't tend to clump up or collide very
        much.  This is good if you are trying to use them as hash keys.

        Another nice thing is that you can manage these keys incrementally.  If, for example, you have a white pawn on
        e5, the key has had "zobrist[PAWN][WHITE][E5]" XOR'd into it.  If you XOR this value into the key again, due
        to the way that XOR works, the pawn is deleted from the key.

        What this means is that if you have a key for the current position, and want to move a white pawn from e5 to
        e6, you XOR in the "white pawn on e5" key, which removes the pawn from e5, and XOR in the "white pawn on e6"
        key, which puts a white pawn on e6.  You are guaranteed to get the same key that you'd get if you started over
        and XOR'd all of the keys for all of the pieces together.

        If you want to switch side to move, you XOR the "change side to move" key in.  You can also manage castling
        and en-passant values the same way.

        The utility of this is that you can create a zobrist key at the root of the search, and keep it current
        throughout the course of the search by updating it in "MakeMove()".

        # Some uses for these keys

        * These Zobrist keys are often used as hash keys.  Hash keys have several uses in chess programs:
        * You can use them to implement a transposition table.  This is a large hash table that allows you to keep track
          of positions that you've seen during the search.  This will let you save work in some cases.  If you are going
          to search a position to depth 9, you can look it up in the transposition table, and if you've already searched
          it to depth 9, you might be able to avoid a long search.  A less obvious use of the main transposition table
          involves improving your move ordering.
        * You can use them to implement pawn structure hashing.  You can keep a key that is created only from the pawns
          that are on the board.  You can do sophisticated analysis of pawn structures, and store the result in a hash
          table for later retrieval.  In practice you end up with relatively few pawn structures that arise from a given
          start position, so the hit rate on this hash table is extremely high, so in essence you get to do all of the
          pawn structure evaluation you want for free.
        * You can make a smaller hash table, which you can use to detect repetitions in the current line, so you can
          detect perpetual check and other repetition draw cases.
        * You can use these to create an opening book that handles transpositions.
     */

    // See https://en.wikipedia.org/wiki/Zobrist_hashing for overview and technique for incremental updates.
    // from https://github.com/bhlangonijr/chesslib/blob/49599909c02fc652b15d89048ec88f8b707facf6/src/main/java/com/github/bhlangonijr/chesslib/Board.java
    private static final long RANDOM_SEED = 49109794719L;
    private static final int ZOBRIST_TABLE_SIZE = 2000;
    private static final long[] zorbistRandomKeys = new long[ZOBRIST_TABLE_SIZE];

    static {
        final XorShiftRandom random = new XorShiftRandom(RANDOM_SEED);
        for (int i = 0; i < ZOBRIST_TABLE_SIZE; i++) {
            long key = random.nextLong();
            zorbistRandomKeys[i] = key;
        }
    }

    private long key;


    /**
     * Returns a Zobrist hash code value for this board. A Zobrist hashing assures the same position returns the same
     * hash value. It is calculated using the position of the pieces, the side to move, the castle rights and the en
     * passant target.
     *
     * @return a Zobrist hash value for this board
     * @see <a href="https://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing in Wikipedia</a>
     */
    public long getZobristKey() {
        return key;
    }

    public ZobristHasher(PlayerColor color, Board board) {
        reset(color, board);
    }

    private long calculateFullZorbistKey(PlayerColor color, Board board, boolean wk, boolean wq, boolean bk, boolean bq, int epT) {
        long hash = computeCastlingRights(0L, wk, wq, bk, bq);

        for (int i = 0; i < 64; i++) {
            Piece piece = board.get(i);
            if (piece != null && piece != Piece.EMPTY)
                hash ^= getPieceSquareKey(piece, i);   // Need to know piece's index (ordinal number 0-12) and the square (0-63)
        }

        hash ^= getSideKey(color);

        if (epT >= 0) {
            hash ^= getEnPassantKey(epT);
        }
        return hash;
    }

    public void updateZobristKeyWithEnPassant(int epT) {
        key ^= getEnPassantKey(epT);
    }

    public void updateCastlingRights(long castlingRights, boolean wk, boolean wq, boolean bk, boolean bq) {
        key ^= computeCastlingRights(castlingRights, wk, wq, bk, bq);
    }

    private long computeCastlingRights(long castlingRights, boolean wk, boolean wq, boolean bk, boolean bq) {
        if (wk) castlingRights ^= getCastleRightKey(1, PlayerColor.WHITE);
        if (wq) castlingRights ^= getCastleRightKey(2, PlayerColor.WHITE);
        if (bk) castlingRights ^= getCastleRightKey(3, PlayerColor.BLACK);
        if (bq) castlingRights ^= getCastleRightKey(4, PlayerColor.BLACK);
        return castlingRights;
    }

    private long getCastleRightKey(int castlingRightOrdinal, PlayerColor color) {
        return zorbistRandomKeys[3 * castlingRightOrdinal + 300 + 3 * color.ordinal()];
    }

    private long getSideKey(PlayerColor side) {
        return zorbistRandomKeys[3 * side.ordinal() + 500];
    }

    private long getEnPassantKey(int enPassantTarget) {
        return zorbistRandomKeys[3 * enPassantTarget + 400];
    }

    private long getPieceSquareKey(Piece piece, int square) {
        return zorbistRandomKeys[57 * piece.getIndex() + 13 * square];
    }

    public void clearZorbistMask(long mask) {
        key &= ~mask;
    }

    public void updateZobristKeyWhenSetting(Piece piece, int square) {
        key ^= getPieceSquareKey(piece, square);
    }

    public void updateZobristKeyFlipPlayer(PlayerColor c) {
        key ^= getSideKey(c);
    }

    public long reset(PlayerColor color, Board board) {
        return key = calculateFullZorbistKey(color, board,
            board.canCastlingWhiteKingSide(),
            board.canCastlingWhiteQueenSide(),
            board.canCastlingBlackKingSide(),
            board.canCastlingBlackQueenSide(),
            board.getEnPassantTarget());
    }
}