package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;

public class MinusOneZeroOneEvaluator implements Evaluator {

    public MinusOneZeroOneEvaluator() {
    }

    @Override
    public int evaluate(Game game, int depth) {
        if(game.isDraw()) return 0;

        return game.getActivePlayerColor().isBlack() ? 1 : -1;
    }

    @Override
    public String toString() {
        return "1/1";
    }
}
