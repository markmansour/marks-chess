package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Game;
import com.stateofflux.chess.model.pieces.Piece;

public interface Evaluator {
    int MATE_VALUE = 100_000;
    int MIN_VALUE = -1_000_000;
    int MAX_VALUE = 1_000_000;

    int evaluate(Game game, int depth);
}
