package com.stateofflux.chess.model;

import com.google.common.collect.ForwardingList;

import java.util.ArrayList;
import java.util.List;

/*
 * Used to store the capture and non-capture moves for a given board.
 */
public class MoveList<E> extends ForwardingList<E> {
    final List<E> delegate;

    public MoveList(List<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    public List<String> asLongSan() {
        List<String> sans = new ArrayList<>();
        delegate.forEach(move -> sans.add(((Move) move).toLongSan()));

        return sans;
    }
}
