package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;

import java.security.SecureRandom;

public abstract class Player {
    protected static final int DEFAULT_SEARCH_DEPTH = 2;
    protected int searchDepth;
    protected PlayerColor color;
    protected Evaluator evaluator;

    public Player(PlayerColor pc, Evaluator evaluator) {
        color = pc;
        this.evaluator = evaluator;
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public PlayerColor getColor() {
        return color;
    }

    public abstract Move getNextMove(Game game);

    public int evaluate(Game game) {
        return evaluate(game, 0);
    }

    public int evaluate(Game game, int depth) {
        return evaluator.evaluate(game, depth, getColor());
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public String toString() {
        return getClass().getSimpleName() + " " + color.toString() + " (depth: " + getSearchDepth() + ", nodes eval: " + evaluator.getNodesEvaluated() + ")";
    }

    public int getBestMoveScore() {
        return color.isWhite() ? 1 : -1;
    }
}
