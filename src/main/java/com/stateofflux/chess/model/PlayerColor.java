package com.stateofflux.chess.model;

public enum PlayerColor {
    BLACK, WHITE, NONE;

    public String toString() {
        return switch (this) {
            case BLACK -> "b";
            case WHITE -> "w";
            case NONE -> "-";
        };
    }

    public PlayerColor otherColor()
    {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
            case NONE -> NONE;
        };
    }
}
