package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.PlayerColor;

public class HumanPlayer extends Player {

    public HumanPlayer(PlayerColor pc) {
        super(pc);
    }

    public Move getNextMove(Game game) {
        return game.generateMoves().get(0);
    }
}
