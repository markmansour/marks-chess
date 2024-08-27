package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;

public interface Evaluator {
    public static int MATE_VALUE = 100_000;
    public static int MIN_VALUE = -1_000_000;
    public static int MAX_VALUE = 1_000_000;

    int evaluate(Game game, int depth);
}
