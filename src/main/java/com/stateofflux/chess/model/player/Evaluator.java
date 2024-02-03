package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;

public interface Evaluator {
    public int evaluate(Game game, int depth);

    public int getNodesEvaluated();
}
