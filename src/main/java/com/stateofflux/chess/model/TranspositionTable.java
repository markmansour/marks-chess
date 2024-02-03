package com.stateofflux.chess.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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
     *
     * @param key
     */
    public record Entry (long key, Move best, int depth, int score) {}

    private final Map<Long, Entry> table;

    public TranspositionTable() {
        this.table = new HashMap<>();
    }

    // --------------------- auto generated delegation methods ---------------------
    public int size() {
        return table.size();
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public boolean containsKey(Object key) {
        return table.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return table.containsValue(value);
    }

    public Object get(Object key) {
        return table.get(key);
    }

    public Object put(Object key, Object value) {
        return null;
    }

    public Object put(long key, Entry value) {
        return table.put(Long.valueOf(key), value);
    }

    public Object remove(Object key) {
        return table.remove(key);
    }

    public void putAll(Map m) {
        table.putAll(m);
    }

    public void clear() {
        table.clear();
    }

    public Set keySet() {
        return table.keySet();
    }

    public Collection values() {
        return table.values();
    }

    public Set<Map.Entry<Long, Entry>> entrySet() {
        return table.entrySet();
    }

    public Object getOrDefault(Object key, Entry defaultValue) {
        return table.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer action) {
        table.forEach(action);
    }

    public void replaceAll(BiFunction function) {
        table.replaceAll(function);
    }

    public Object putIfAbsent(Long key, Entry value) {
        return table.putIfAbsent(key, value);
    }

    public boolean remove(Object key, Object value) {
        return table.remove(key, value);
    }

    public boolean replace(Long key, Entry oldValue, Entry newValue) {
        return table.replace(key, oldValue, newValue);
    }

    public Object replace(Long key, Entry value) {
        return table.replace(key, value);
    }

    public Object computeIfAbsent(Long key, Function mappingFunction) {
        return table.computeIfAbsent(key, mappingFunction);
    }

    public Object computeIfPresent(Long key, BiFunction remappingFunction) {
        return table.computeIfPresent(key, remappingFunction);
    }

    public Object compute(Long key, BiFunction remappingFunction) {
        return table.compute(key, remappingFunction);
    }

    public Object merge(Long key, Entry value, BiFunction remappingFunction) {
        return table.merge(key, value, remappingFunction);
    }

}
