package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Move;

import java.util.*;
import java.util.stream.Collectors;

public class MoveHistory {
    private Move bestMove;
    private int score;
    private Move[] previousMoves;
    private static final Move[] EMPTY_MOVE_LIST = new Move[0];

    public MoveHistory(Move bestMove, int score) {
        this.bestMove = bestMove;
        this.score = score;
        this.previousMoves = EMPTY_MOVE_LIST;
    }

    public void addMove(Move newMove, int newScore) {
        Move[] list;
        if(previousMoves == null || previousMoves.length == 0) {
            previousMoves = new Move[1];
            previousMoves[0] = bestMove;
        } else {
            list = new Move[previousMoves.length + 1];
            System.arraycopy(previousMoves, 0, list, 1, previousMoves.length);
            list[0] = bestMove;
            previousMoves = list;
        }

        bestMove = newMove;
        score = newScore;
    }

    public Move bestMove() {
        return bestMove;
    }

    public int score() {
        return score;
    }

    public Move[] previousMoves() {
        return previousMoves;
    }

    public String previousMovesToString() {
        if(previousMoves == null)
            return "";

        return Arrays.stream(previousMoves).map(Move::toLongSan).collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MoveHistory) obj;
        return Objects.equals(this.bestMove, that.bestMove) &&
            this.score == that.score &&
            Objects.equals(this.previousMoves, that.previousMoves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bestMove, score, previousMoves);
    }

    @Override
    public String toString() {
        return "MoveHistory[" +
            "bestMove=" + bestMove + ", " +
            "score=" + score + ", " +
            "previousMoves=" + previousMoves + ']';
    }
}
