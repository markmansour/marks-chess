package com.stateofflux.chess.model;

public enum Direction {
    UP (8),
    DOWN (-8),
    LEFT (1),
    RIGHT (-1),

    UP_LEFT (9),
    UP_RIGHT (7),
    DOWN_LEFT (-7),
    DOWN_RIGHT (-9);

    private final int distance;

    Direction(int distance) {
        this.distance = distance;
    }

    public int getDistance() { return this.distance; }
}