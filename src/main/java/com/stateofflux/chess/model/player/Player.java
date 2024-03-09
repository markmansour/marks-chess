package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;

public abstract class Player {
    protected static final int DEFAULT_SEARCH_DEPTH = 2;
    protected int searchDepth;
    protected PlayerColor color;
    protected Evaluator evaluator;
    protected int nodesVisited;

    public Player(PlayerColor pc, Evaluator evaluator) {
        color = pc;
        this.evaluator = evaluator;
        nodesVisited = 0;
        searchDepth = DEFAULT_SEARCH_DEPTH;
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public PlayerColor getColor() {
        return color;
    }

    public abstract Move getNextMove(Game game);

    public int evaluate(Game game, int depthTraversed) {
        return evaluator.evaluate(game, depthTraversed);
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public String toString() {
        return getClass().getSimpleName() + " (" + color.toString() + ", depth: " + getSearchDepth() + ")";
    }

    public int getNodesVisited() {
        return nodesVisited;
    }

    public void reset() {
        nodesVisited = 0;
    }

    public void setHashInMb(int hashSize) {
        // no-op
    }

    public void setIncrement(long increment) {
        // no-op
    }

    public Object getEvaluator() {
        return evaluator;
    }
}
