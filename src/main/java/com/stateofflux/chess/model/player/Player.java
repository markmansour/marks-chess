package com.stateofflux.chess.model.player;

import java.util.HashMap;
import java.util.Map;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;
import com.stateofflux.chess.model.pieces.Piece;

public abstract class Player {
    protected PlayerColor color;

    public abstract Move getNextMove(Game game);
}
