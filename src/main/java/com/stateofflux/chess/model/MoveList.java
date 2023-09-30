package com.stateofflux.chess.model;

import com.google.common.collect.ForwardingList;

import java.util.ArrayList;
import java.util.List;

public class MoveList<E> extends ForwardingList<E> {
    final List<E> delegate;

    MoveList(List<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    // TODO: Memoize this result
    public List<String> asLongSan() {
        List<String> sans = new ArrayList<>();
        delegate.forEach(move -> sans.add(((Move) move).toLongSan()));

        return sans;
    }
}
