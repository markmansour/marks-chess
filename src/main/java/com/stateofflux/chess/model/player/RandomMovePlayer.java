package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.MoveList;
import com.stateofflux.chess.model.PlayerColor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.security.SecureRandom;

public class RandomMovePlayer extends Player {
    protected final SecureRandom rand;

    @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
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
