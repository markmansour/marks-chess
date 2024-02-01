package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;

public interface Evaluator {
    public int evaluate(Game game, int depth, PlayerColor pc);

    public int getNodesEvaluated();
}
