# Mark's Chess

The project contains a chess engine with following abilities:
* understands all valid moves including castling, en passant, and promotion
* understands checkmate, stalemate, the 50 rule move, insufficient materials, and repetition.
* basic UCI interface
* perft to depth 6 tested and valid
* uses bitboards for speed, with perft 6 running at 2.9MM node/second on my M2 macbook air.
* Two working players.  
  * RandomMovePlayer - generates moves to a depth of 1 and randomly picks
  * BasicNegaMaxPlayer - uses nagamax (minimax) with a [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) 
    (values in centipawns) and [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables).


Next steps
* opening books
* end games
* calculate the rough ELO for my engine
* Player with alpha/beta pruning
* move ordering
* multi-threading
* AI chess player (neural nets)
* resignations

## Build
IntelliJ project included.  

If you're doing this on the command line then to get started:

```bash
$ git clone https://github.com/markmansour/marks-chess.git
$ cd marks-chess
$ mvn package
```

## Goals
## Non-goals

## Playing against other engines
Build a single jar file with all dependencies, and call it from a shell script.  That
shell script can be called from a chess engine.

Create a package:
```bash
$ mvn compile exec:java -Dexec.mainClass=com.stateofflux.chess.App
```

Create a OS launcher (shell script) containing..
```bash
/bin/bash -c 'cd <PATH>/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App'
```
You'll need to make it executable (chmod +x <shell script>).

I've added marks-chess to [cutechess](https://github.com/cutechess/cutechess) on MacOS and had it compete against itself, a human, and stockfish.


