package com.stateofflux.chess.model;

public enum MoveFlag {
    CAPTURE('c'),
    NONCAPTURE('n');

    private final char flag;

    MoveFlag(char flag) {
        this.flag = flag;
    }
}
