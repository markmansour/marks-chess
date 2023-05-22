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
}
