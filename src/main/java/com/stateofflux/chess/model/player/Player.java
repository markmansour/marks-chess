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
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public PlayerColor getColor() {
        return color;
    }

    public abstract Move getNextMove(Game game);

    public int evaluate(Game game, PlayerColor pc) {
        return evaluate(game, 0);
    }

    public int evaluate(Game game, int depth) {
        return evaluator.evaluate(game, depth);
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public String toString() {
        return getClass().getSimpleName() + " " + color.toString() + " (depth: " + getSearchDepth() + ", nodes eval: ";
    }

    public int getBestMoveScore() {
        return color.isWhite() ? 1 : -1;
    }

    public int getNodesVisited() {
        return nodesVisited;
    }
}
