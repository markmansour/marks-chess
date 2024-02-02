package com.stateofflux.chess.model.writer;

import com.google.common.graph.MutableValueGraph;
import com.stateofflux.chess.model.Move;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


public class SimpleDotWriter {
    private final StringBuilder sb;
    private final Set<String> visited;

    public SimpleDotWriter() {
        sb = new StringBuilder();
        visited = new HashSet<>();
    }

    private static String arrayToKey(Move[] moves) {
        String[] sans = new String[moves.length];
        int j = 0;

        for(int i = moves.length - 1; i >= 0; i--) {
            sans[j++] = moveToString(moves[i]);
        }

        return String.join("_", sans);
    }

    private static String moveToString(Move m) {
        char c = m.getPiece().getPieceChar();
        if(c == ' ') c = 'X';
        return c + "-" + m.toLongSan();
    }

    private void traverseSuccessors(final MutableValueGraph<Move, Integer> graph, Move[] head, int depth) {
        Set<Move> moves = graph.successors(head[head.length - 1]);  // last item in the array

        // when we're at a leaf - print pairs of nodes and highlight in red the important ones.
        if(moves.isEmpty()) {
            for(int i = 0; i < head.length - 1; i++) {
                String currentNode = arrayToKey(Arrays.copyOf(head, i + 1));
                String nextNode = arrayToKey(Arrays.copyOf(head, i + 2));
                String key = currentNode + "_" + nextNode;
                Move importantChild = getImportantChild(graph, head[i], i);

                graph.predecessors(head[i + 1]);

                if(visited.contains(key))
                    continue;

                visited.add(key);
                int edgeValue = graph.edgeValue(head[i], head[i+1]).get();
                sb.append(currentNode)
                    .append(" -> ")
                    .append(nextNode)
                    .append(" [label=\"")
                    .append(edgeValue);

                if(importantChild.equals(head[i+1]))
                    sb.append("\" color=\"red");

                sb.append("\"];\n");
                sb.append(nextNode).append(" [label=\"").append(head[i+1]).append("\"];\n");
            }

            return;
        }

        // write the node itself

        for(Move m : moves) {
            Move[] newMoves = new Move[head.length + 1];
            System.arraycopy(head, 0, newMoves, 0, head.length);
            newMoves[head.length] = m;
            traverseSuccessors(graph, newMoves, depth + 1);
        }
    }

    private Move getImportantChild(MutableValueGraph<Move, Integer> graph, Move move, int depth) {
        Set<Move> moves = graph.successors(move);
        Move bestMove = null;

        int score;
        if(depth % 2 == 0)
            score = Integer.MIN_VALUE; // maximize - white
        else
            score = Integer.MAX_VALUE; // minimize - black

        for(Move child: moves) {
            int edgeValue = graph.edgeValue(move, child).get();
            if(depth % 2 == 0) {
                if(edgeValue > score) {
                    score = edgeValue;
                    bestMove = child;
                }
            } else {
                if(edgeValue < score) {
                    score = edgeValue;
                    bestMove = child;
                }
            }

        }

        return bestMove;
    }

    public String fromGraph(final MutableValueGraph<Move, Integer> graph, Move root) {
        sb.append("strict digraph G {\n");
        sb.append(moveToString(root)).append(" [label\"root\"];\n");
        // traverseSuccessors(graph, new Move[] {root}, 0);
        dfs(graph, root);
        sb.append("}");
        return sb.toString();
    }

    private String dfs(MutableValueGraph<Move, Integer> graph, Move parent) {
        Set<Move> children = graph.successors(parent);
        String parentString = moveToString(parent) + "_" + ThreadLocalRandom.current().nextInt(0, 1000);

        if(children.isEmpty()) {
            sb.append(parentString)
                .append(" [label=\"")
                .append(moveToString(parent))
                .append("\"];\n");
            return parentString;
        }

        for(Move child : children) {
            String childString = dfs(graph, child);
            sb.append(parentString)
                .append(" -> ")
                .append(childString)
                .append(" [label=\"")
                .append(child.toLongSan())
                .append("\"];\n");
        }

        return parentString;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public boolean toFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false))){
            writer.write(toString());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
