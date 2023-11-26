package com.stateofflux.chess.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pgn {
    Map<String, String> tagPairs = new HashMap<>();
    record PgnMove(int moveNumber, String whiteMove, String blackMove, String comment) { }
    List<PgnMove> moves;

    // https://www.chessclub.com/help/PGN-spec
    public Pgn(String raw) {
        String line;
        //These continuation characters are letter characters ("A-Za-z"), digit characters
        //("0-9"), the underscore ("_"), the plus sign ("+"), the octothorpe sign ("#"),
        //the equal sign ("="), the colon (":"),  and the hyphen ("-").
        Pattern tagPairsPattern = Pattern.compile("\\s*\\[\\s*([A-Za-z0-9_+#=:-]+)\\s+\"(.+)\"\\s*\\]");
        try (BufferedReader br = new BufferedReader(new StringReader(raw))) {
            moves = new ArrayList<>();

            line = br.readLine();

            // eat leading whitespaces
            while(line.isBlank())
                line = br.readLine();

            // phase 1 - read the tag pairs (headers)
            while(true) {
                if ((line.startsWith("%"))) continue;  // escape char for proprietary use - ignore the line
                if ((line.isBlank())) break;  // exit the tag pair section and start the move text section
                Matcher m = tagPairsPattern.matcher(line);
                boolean found = m.find();
                if (!found) continue;
                if (m.groupCount() != 2)
                    throw new IllegalArgumentException("wrong number of groups found for \"" + line + "\": " + m.groupCount());

                tagPairs.put(m.group(1), m.group(2));
                if ((line = br.readLine()) == null) throw new IllegalArgumentException("raw string terminated before list of moves");  // end of file, this is an error
            }

            // phase 2 - movetext section
            StringBuilder moveTextSb = new StringBuilder();

            while(true) {
                if ((line = br.readLine()) == null) break; // end of the file
                if ((line.isBlank())) break;  // end of the game
                // trim comments in the form of "{}" or ";"
                line = line.replaceAll("[{].*[}]", "");  // throw away comments
                line = line.replaceAll(";.*$", "");  // throw away comments
                moveTextSb.append(line).append(' ');
            }

            String moveText = moveTextSb.toString();
            moveText = moveText.replaceAll(tagPairs.get("Result")+"\\s+$", "");  // remove the result from the end fo the string.
            String[] tokens = moveText.split("\\s+");

            int index = 0;
            String whiteMove = "";
            String blackMove = "";

            while(index < tokens.length) {
                if (index + 1 < tokens.length) whiteMove = tokens[index + 1];
                blackMove =  (index + 2 < tokens.length) ? tokens[index + 2] : "";
                moves.add(new PgnMove(index == 0 ? 1 : (index / 3) + 1, whiteMove, blackMove, ""));
                index += 3;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
