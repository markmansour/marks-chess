package com.stateofflux.chess.model.player;

import com.stateofflux.chess.model.Move;
import com.stateofflux.chess.model.TranspositionTable;

import java.util.*;
import java.util.stream.Collectors;

public class MoveHistory {
    private final Move bestMove;
    private final int score;
    private final Deque<Move> previousMoves;

    public MoveHistory(Move bestMove, int score) {
        this.bestMove = bestMove;
        this.score = score;
        this.previousMoves = new ArrayDeque<>(1);
    }

    private MoveHistory(Move bestMove, int score, Deque<Move> list) {
        this.bestMove = bestMove;
        this.score = score;
        this.previousMoves = list;
    }

    MoveHistory(TranspositionTable.Entry entry) {
        this.bestMove = entry.getBestMove();
        this.score = entry.score();
        this.previousMoves = new ArrayDeque<>(1);
    }

    public MoveHistory createNew(Move newMove, int newScore) {
        ArrayDeque<Move> list = new ArrayDeque<>(previousMoves.size() + 1);
//        if(previousMovesToString().contains(newMove.toLongSan()))
//            newMove.toLong();

        list.addAll(previousMoves);
        list.addLast(bestMove);

        return new MoveHistory(newMove, newScore, list);
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

    public String previousMovesToString() {
        return previousMoves().stream().map(Move::toLongSan).collect(Collectors.joining(" ")).toString();
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
