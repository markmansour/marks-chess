package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;

import java.util.concurrent.ThreadLocalRandom;

public class RandomMovePlayer extends Player {
    private Game game;
    private int nodesEvaluted;

    public RandomMovePlayer(PlayerColor pc) {
        super(pc);
        nodesEvaluted = 0;
    }

    public Move getNextMove(Game game) {
        this.game = game;
        MoveList<Move> moves = game.generateMoves();
        nodesEvaluted++;
        return moves.get(rand.nextInt(moves.size()));
    }

    @Override
    public int getNodesEvaluated() {
        return nodesEvaluted;
    }
}
