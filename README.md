# Mark's Chess
[![CI workflow](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml/badge.svg)](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/markmansour/marks-chess/actions/workflows/build.yml)

This is a straight forward chess engine written in Java.  The chess engine comes with a basic UCI interface.
There are thorough unit tests.  There is *no* GUI.

## Chess Engine Capabilities
The project contains a chess engine with following abilities:
* Understands all valid moves including castling, en passant, and promotion.
* Understands checkmate, stalemate, the 50 rule move, insufficient materials, and repetition.
* Basic UCI interface.
* Uses bitboards (and
  [Magic bitboards](https://rhysre.net/fast-chess-move-generation-with-magic-bitboards.html)) for speed.
* Perft 7 running at 4MM nodes/second on my M2 macbook air (in comparison to 36MM from
  [QBB Perft](https://github.com/lithander/QBB-Perft/tree/master) a highly optimized and specific
  implementation).
* Four working players.
  * [RandomMovePlayer](src/main/java/com/stateofflux/chess/model/player/RandomMovePlayer.java) - generates
    moves to a depth of 1 and randomly picks a move.
  * [BasicNegaMaxPlayer](src/main/java/com/stateofflux/chess/model/player/BasicNegaMaxPlayer.java) - uses
    negamax (minimax) with a
    [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) (values in
    centipawns) and
    [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables).
  * [AlphaBetaPlayer](src/main/java/com/stateofflux/chess/model/player/AlphaBetaPlayer.java) with alpha/beta
    pruning (Simple evaluation and PSTs).
  * [AlphaBetaPlayerTT](src/main/java/com/stateofflux/chess/model/player/AlphaBetaPlayerWithTT.java) with
    alpha/beta search, iterative deepening, time control, and transposition tables.
* Move ordering (basic version implemented to prioritize captures) and improved move ordering
  ([MVV-LVA](https://www.chessprogramming.org/MVV-LVA)).
* Zobrist keys to find repetition and support speed improvements during search.
* Iterative Deepening and Time management

## Design
* **Game** - contains only game state such as clock moves, draw/checked/mate/repetition/50 move rule, the
  active player, and a history of moves.  Players make moves in the game object, which updates the underlying
  board.  The Game object deals in moves including move generation and enforces correctness of moves.
* **Board** - this class represents the board layout including where pieces are placed, and hashes of the
  board state.  It does not enforce correctness.
* **Pieces** - com.stateofflux.chess.model.pieces.*: This package attempts to encapsulate all piece logic.
* **Players** - logic to determine how to move pieces on the board.  Includes search and evaluation functions.
  In traditional chess engines this is called artificial intelligence.

## Build Built against
[Amazon Corretto JDK 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/what-is-corretto-17.html)
and Apache Maven 3.9+.

If you're building the project on the command line then to get started:


```bash
$ git clone https://github.com/markmansour/marks-chess.git
$ cd marks-chess $ mvn package assembly:single
$ ./bin/app.sh
```

## Playing against other engines 
Build a single jar file with all dependencies, and call it from a shell
script.  That shell script can be called from a chess engine.  See [debugging](docs/debugging.md) and
[tournament testing](docs/tournament%20testing%20marks%20chess.md) for more configuration options.

```bash
$ ./bin/app.sh
```

I use this script as a launch for GUIs such as [cutechess](https://github.com/cutechess/cutechess) or banksia
on macOS.

# Why?  
## Goals
* Have fun!  It's been years since I've regularly programmed and this project is my way of re-engaging with
  programming.  And there are 1000s of chess engines in existence, so I have absolutely no intention of
  commercializing this code.
* Re-familiarize myself with the Java ecosystem (including AssertJ, JUnit, TestNG, Google Guava, SpotBugs,
  Maven, etc.).
* Experiment with benchmarking frameworks (using
  [async profiler](https://github.com/async-profiler/async-profiler)).
* Experiment with 3rd party services (such as GitHub actions)
* Move from theory to practice with Neural Nets.

## Non-goals
* Make a competitive chess engine.  If it happens, then that's a nice (but unexpected) bonus.
* Write production quality code.  This is for fun, and I'm *sure* my classes could be factored in a much
  cleaner way.
