package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.PlayerColor;

public class MinusOneZeroOneEvaluator implements Evaluator {

    public MinusOneZeroOneEvaluator() {
    }

    @Override
    public int evaluate(Game game, int depth, PlayerColor pc) {
        if(game.isDraw()) return 0;

        return pc.isWhite() ? 1 : -1;
    }

    @Override
    public int getNodesEvaluated() {
        return 0;
    }
}
