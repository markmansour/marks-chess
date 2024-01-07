package com.stateofflux.chess.model;

import java.util.HashMap;
import java.util.Map;

import com.stateofflux.chess.model.pieces.Piece;

public abstract class Player {
    protected PlayerColor color;

    public abstract Move getNextMove(Game game);
}
