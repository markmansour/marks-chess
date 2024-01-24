package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;

import java.util.concurrent.ThreadLocalRandom;

public class RandomMovePlayer extends Player {
    private Game game;

    public RandomMovePlayer(PlayerColor pc) {
        super(pc);
    }

    public Move getNextMove(Game game) {
        this.game = game;
        MoveList<Move> moves = game.generateMoves();
        return moves.get(rand.nextInt(moves.size()));
    }
}
