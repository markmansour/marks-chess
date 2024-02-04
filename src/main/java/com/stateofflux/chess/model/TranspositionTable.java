package com.stateofflux.chess.model;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.stateofflux.chess.model.pieces.KingMoves.MATE_VALUE;

public class TranspositionTable {

    /**
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

    private final int hashSize;
    private final long[] data;
    private final long[] keys;
    private final int mask;

    public record Entry (long key, Move best, int depth, int score, NodeType nt, int age) {}

    public TranspositionTable() {
        this(128);
    }

    public TranspositionTable(int memoryUsageInMB) {
        int entrySizeInBytes = 8;  // packing all data into a long (64 bits / 8 bytes);
        hashSize = (memoryUsageInMB * 1024 * 1024) / entrySizeInBytes ;  // number of entries
        mask = hashSize - 1; // m = n & (d - 1) will compute the modulo (base 2) where n -> numerator, d -> denominator, m is modulo.
        data = new long[hashSize];
        keys = new long[hashSize];
    }

    public enum NodeType {
        EXACT, LOWER_BOUND, UPPER_BOUND;
    }

    /*
        fun get(key: Long, ply: Int): Entry? {

            val k = keys[key.and(mask).toInt()]
            val d = data[key.and(mask).toInt()]
            val entry = buildEntry(k, d, ply)

            return if ((k xor d) == key) entry else null
        }
    */
    public static int long2int(long l) {
        return (int) (l & 0xFFFFFFFFL); // (1L << 32) - 1 => 0xFFFFFFFFL

    }
    public Entry get(long key, int ply) {
        long k = keys[Math.toIntExact(key & mask)];
        long d = data[Math.toIntExact(key & mask)];
        Entry e = buildEntry(key, d, ply);

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
    public boolean put(long key, int score, int depth, NodeType nodeType, int ply) {
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
            int index = Math.toIntExact(key & mask); // key & mask modulos the key
            keys[index] = key ^ d;  // i don't understand why this is xor-ed.  Assuming it is a checksum, but it means the key returned in the Entry object is a corruption of the original key.
            data[index] = d;
            return true;
        }

        return false;
    }

    public void clear() {
        Arrays.fill(keys, 0L);
        Arrays.fill(data, 0L);
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
    private Entry buildEntry(long key, long data, int ply){
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

        return new Entry(key, null, depth, newScore, nodeType, 0);
    }
}
