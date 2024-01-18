package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;

public abstract class Player {
    protected static final int DEFAULT_SEARCH_DEPTH = 2;
    protected PlayerColor color;
    protected int searchDepth;

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public abstract Move getNextMove(Game game);
}
