package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMovePlayer extends Player {
    protected final SecureRandom rand;

    public RandomMovePlayer(PlayerColor pc, Evaluator evaluator) {
        super(pc, evaluator);
        rand = new SecureRandom();
        rand.setSeed(123456789L);  // for reproducible testing    public RandomMovePlayer(PlayerColor pc, Evaluator evaluator) {
    }

    public Move getNextMove(Game game) {
        MoveList<Move> moves = game.generateMoves();
        return moves.get(rand.nextInt(moves.size()));
    }
}
