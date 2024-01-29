package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;

import java.security.SecureRandom;

public abstract class Player {
    protected static final int DEFAULT_SEARCH_DEPTH = 2;
    protected int searchDepth;
    protected final SecureRandom rand;
    protected PlayerColor color;

    public Player(PlayerColor pc) {
        rand = new SecureRandom();
        rand.setSeed(123456789L);  // for reproducible testing
        color = pc;
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public abstract Move getNextMove(Game game);

    public int evaluate(Game game) {
        return color.isWhite() ? 1 : -1;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public abstract int getNodesEvaluated();

    public String toString() {
        return getClass().getSimpleName() + " " + color.toString() + " (depth: " + getSearchDepth() + ", nodes eval: " + getNodesEvaluated() + ")";
    }
}
