package com.stateofflux.chess.model;

import java.util.concurrent.ThreadLocalRandom;

public class RandomMovePlayer extends Player {
    public RandomMovePlayer(PlayerColor color) {
        this.color = color;
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
    }

    public String toString() {
        return "RandomMovePlayer: " + color;
    }
}
