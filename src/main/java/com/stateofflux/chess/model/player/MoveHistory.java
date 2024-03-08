package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Move;

import java.util.*;
import java.util.stream.Collectors;

public class MoveHistory {
    private Move bestMove;
    private int score;
    private Deque<Move> previousMoves;

    public MoveHistory(Move bestMove, int score) {
        this.bestMove = bestMove;
        this.score = score;
        this.previousMoves = new ArrayDeque<>(8);
    }

    public void addMove(Move newMove, int newScore) {
        previousMoves.addLast(bestMove);
        bestMove = newMove;
        score = newScore;
    }

    public Move bestMove() {
        return bestMove;
    }

    public int score() {
        return score;
    }

    public Deque<Move> previousMoves() {
        return previousMoves;
    }

    public String pv() {
        if(previousMoves == null)
            return "";

        StringBuilder sb = new StringBuilder(bestMove.toLongSan());
        Iterator<Move> i = previousMoves.descendingIterator();

        while(i.hasNext()) {
            sb.append(" ").append(i.next().toLongSan());
        }

        return sb.toString();
    }

    public String previousMovesToString() {
        if(previousMoves == null)
            return "";

        StringBuilder sb = new StringBuilder();
        Iterator<Move> i = previousMoves.descendingIterator();

        while(i.hasNext()) {
            sb.append(i.next().toLongSan());

            if(i.hasNext()) sb.append(" ");
        }

        return sb.toString();
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
