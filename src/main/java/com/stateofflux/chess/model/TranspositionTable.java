package com.stateofflux.chess.model;

import java.util.*;

import static com.stateofflux.chess.model.player.Evaluator.MATE_VALUE;

public class TranspositionTable {

    /*
     * From https://www.chessprogramming.org/Transposition_Table
     *
     * What Information is Stored
     * Typically, the following information is stored as determined by the search [17] :
     * - Zobrist- or BCH-key, to look whether the position is the right one while probing
     * - Best- or Refutation move
     * - Depth (draft)
     * - Score, either with Integrated Bound and Value or otherwise with
     * - Type of Node [18]
     *   - PV-Node, Score is Exact
     *   - All-Node, Score is Upper Bound
     *   - Cut-Node, Score is Lower Bound
     * - Age is used to determine when to overwrite entries from searching previous positions during the game of chess
     */

    private final long[] keys;
    private final long[] data;
    private final long[] movesData;
    private final int mask;
    private final int maxEntries;  // number of entries
    private int activeEntries;

    public static final int DEFAULT_HASH_SIZE_IN_MB = 128;

    public record Entry(long key, int score, long best, NodeType nt, int depth, int age) {
        public Move getBestMove() {
            return Move.buildFrom(best);
        }
    }

    public TranspositionTable() {
        this(DEFAULT_HASH_SIZE_IN_MB);
    }

    public TranspositionTable(int memoryUsageInMB) {
        int entrySizeInBytes = 8;  // packing all data into a long (64 bits / 8 bytes);
        maxEntries = (memoryUsageInMB * 1024 * 1024) / entrySizeInBytes;
        activeEntries = 0;
        mask = maxEntries - 1; // m = n & (d - 1) will compute the modulo (base 2) where n -> numerator, d -> denominator, m is modulo.
        data = new long[maxEntries];
        keys = new long[maxEntries];
        movesData = new long[maxEntries];
    }

    public enum NodeType {
        EXACT, LOWER_BOUND, UPPER_BOUND
    }

    /*
    fun get(key: Long, ply: Int): Entry? {

        val k = keys[key.and(mask).toInt()]
        val d = data[key.and(mask).toInt()]
        val entry = buildEntry(k, d, ply)

        return if ((k xor d) == key) entry else null
    }
    */
    public Entry get(long key, int ply) {
        long k = keys[Math.toIntExact(key & mask)];
        long d = data[Math.toIntExact(key & mask)];
        long md = movesData[Math.toIntExact(key & mask)];
        Entry e = buildEntry(key, d, md, ply);

        if ((k ^ d) == key) {
            return e;
        }

        return null;
    }

    /*
    fun put(key: Long, value: Long, depth: Int, nodeType: NodeType, ply: Int): Boolean {

        val entry = get(key, ply)
        if (entry == null || depth > entry.depth || nodeType == NodeType.EXACT) {
            val newValue = when {
                value >= MATE_VALUE -> value + ply
                value <= -MATE_VALUE -> value - ply
                else -> value
            }
            val d = buildData(newValue, depth, nodeType)
            keys[key.and(mask).toInt()] = key xor d
            data[key.and(mask).toInt()] = d
            return true
        }
        return false
    }
    */
    public boolean put(long key, int score, Move best, NodeType nodeType, int depth, int ply) {
        Entry entry = get(key, ply);
        if(entry == null || depth > entry.depth || nodeType == NodeType.EXACT) {
            int newScore;
            if(score >= MATE_VALUE)
                newScore = score + ply;
            else if(score <= -MATE_VALUE)
                newScore = score - ply;
            else
                newScore = score;

            long d = buildData(newScore, depth, nodeType);
            int index = Math.toIntExact(key & mask); // key & mask will take the modulo of the key

            // key ^ d acts as a checksum.  It is possible to generate the same key twice, so xor it with d.  If
            // reversing the xor is the original key, then it was the value inserted into the array.
            keys[index] = key ^ d;
            data[index] = d;
            movesData[index] = best.toLong();
            activeEntries++;

            return true;
        }

        return false;
    }

    public void clear() {
        Arrays.fill(keys, 0L);
        Arrays.fill(data, 0L);
        activeEntries = 0;
    }

    private long buildData(int score, int depth, NodeType nodeType) {
        // pack score, depth, and node type into a long.  There is quite a lot of spare room in the long to encode
        // other attributes.
        return (((long) score & 0xFFFFFFFFL) << 32) |  // (1L << 32) - 1 => 0xFFFFFFFFL
            (((long) depth & 0xFFFFL) << 16) |         // (1L << 16) - 1 => 0xFFFFL
            ((long) nodeType.ordinal()) & 0xFFFFL;
    }


    /*
        private val nodeValues = NodeType.values()

        private fun buildEntry(key: Long, data: Long, ply: Int): Entry {

            val value = data.ushr(32).and(0xFFFFFFFFL).toInt().toLong()
            val depth = data.ushr(16).and(0xFFFFL).toShort().toInt()
            val nodeType = nodeValues[data.and(0xFFFFL).toInt()]
            val newValue = when {
                value > MATE_VALUE -> value - ply
                value < -MATE_VALUE -> value + ply
                else -> value
            }
            return Entry(key, newValue, depth, nodeType)
        }
    */
    private Entry buildEntry(long key, long data, long moveData, int ply){
        int score = ((int) ((data >> 32) & 0xFFFFFFFFL));
        int depth = ((int) ((data >> 16) & 0xFFFFL));
        NodeType nodeType = NodeType.values()[(int) (data & 0xFFFFL)];

        int newScore;
        if(score > MATE_VALUE)
            newScore = score - ply;
        else if(score < -MATE_VALUE)
            newScore = score + ply;
        else
            newScore = score;

        return new Entry(key, newScore, moveData, nodeType, depth, 0);
    }

    public int getActiveEntries() {
        return activeEntries;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    // 0 is empty, 1000 is full
    public int getHashfull() {
        return (1000 * activeEntries) / maxEntries;
    }
}
