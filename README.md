# Mark's Chess
[![CI workflow](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml/badge.svg)](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/markmansour/marks-chess/actions/workflows/build.yml)

This is a straight forward chess engine written in Java.  The chess engine comes with a basic UCI interface.  There are thorough unit tests.  There is *no* GUI.

## Chess Engine Capabilities
The project contains a chess engine with following abilities:
* Understands all valid moves including castling, en passant, and promotion.
* Understands checkmate, stalemate, the 50 rule move, insufficient materials, and repetition.
* Basic UCI interface.
* Uses bitboards (and [Magic bitboards](https://rhysre.net/fast-chess-move-generation-with-magic-bitboards.html)) for speed.  This was a lot of fun to write.
* With perft 7 running at 4MM nodes/second on my M2 macbook air.
* Three working players.  
  * RandomMovePlayer - generates moves to a depth of 1 and randomly picks a move.
  * BasicNegaMaxPlayer - uses nagamax (minimax) with a [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) 
    (values in centipawns) and [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables).
  * AlphaBetaPlayer with alpha/beta pruning (Simple evaluation and PSTs)
* Move ordering (basic version implemented to prioritize captures)
* Zobrist keys to find repetition

### Next steps
* Improved search (Iterative deepening, widening aspiration windows, quiescence, Transposition Tables)
* Pruning and Reduction (late move reduction, null moves, futility pruning)
* Improved evaluation (king attacked)
* Improved move ordering 
* Opening books (See [lichess openings](https://github.com/lichess-org/chess-openings/tree/master)), [BALSA](https://sites.google.com/site/computerschess/balsa-opening-test-suite), [beginners guide](http://horizonchess.com/FAQ/Winboard/openingbook.html), [Polyglot book format](http://hgm.nubati.net/book_format.html), [Chess Stack Exchange](https://chess.stackexchange.com/questions/5933/how-to-create-your-own-opening-book-for-your-own-chess-engine) thread, [Bluefever software video](https://www.youtube.com/watch?v=hGy5kR_mOdM)   
* End games
* Calculate the rough [Elo](https://en.wikipedia.org/wiki/Elo_rating_system) for my engine.  Tools to review: [Openbench](https://github.com/AndyGrant/OpenBench), [Chess Tuning Tool](https://chess-tuning-tools.readthedocs.io/en/latest/index.html) for tournament simulation.
* Compare my chess engine against other engines using [Computer Chess Rating Lists](http://computerchess.org.uk/ccrl/).  Currently trying to beat "[Dumb](https://github.com/abulmo/Dumb/tree/master)" but not succeeding as it's Elo rating is 2698 (my bot isn't close to this (yet!))
* 
* Multi threaded search and evaluation
* Time management
* AI chess player (neural nets) - see [maia](https://github.com/CSSLab/maia-chess), [sunfish](https://github.com/thomasahle/sunfish), [Bagatur](https://github.com/bagaturchess/Bagatur) (Java).  Perhaps [bullet](https://github.com/jw1912/bullet) would be useful for training?
* Resignations

### Maybes
* Monte Carlo Tree search
* Tournament Framework

## Design
* **Game** - contains only game state such as clock moves, draw/checked/mate/repetition/50 move rule, the active player, and a history of moves.  Players make moves in the game object, which updates the underlying board.  The Game object deals in moves including move generation and enforces correctness of moves.
* **Board** - this class represents the board layout including where pieces are placed, and hashes of the board state.  It does not enforce correctness.
* **Pieces** - com.stateofflux.chess.model.pieces.*: This package attempts to encapsulate all piece logic.
* **Players** - logic to determine how to move pieces on the board.  Includes search and evaluation functions.

## Build
IntelliJ project included.  Currently built against [Amazon Corretto JDK 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/what-is-corretto-17.html) and Apache Maven 3.9+.

If you're doing this on the command line then to get started:

```bash
$ git clone https://github.com/markmansour/marks-chess.git
$ cd marks-chess
$ mvn package
```

## Playing against other engines
Build a single jar file with all dependencies, and call it from a shell script.  That
shell script can be called from a chess engine.

Create a package:
```bash
$ mvn package assembly:single
```

Create an OS launcher (shell script) containing.
```bash
/bin/bash -c 'cd <PATH>/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App'
```
You'll need to make it executable (chmod +x <shell script>).

I've added marks-chess to [cutechess](https://github.com/cutechess/cutechess) on macOS and had it compete against itself, a human, and stockfish.


# Why?
## Goals
* Have fun!  It's been years since I've regularly programmed and this project is my way of re-engaging with programming.  And there are 1000s of chess engines in existence, so I have absolutely no intention of commercializing this code.
* Re-familiarize myself with the Java ecosystem (including AssertJ, JUnit, TestNG, Google Guava, SpotBugs, Maven, etc.).
* Experiment with benchmarking frameworks (using [async profiler](https://github.com/async-profiler/async-profiler)).
* Experiment with 3rd party services (such as GitHub actions)
* Move from theory to practice with Neural Nets.

## Non-goals
* Make a competitive chess engine.  If it happens, then that's a nice (but unexpected) bonus.
* Write production quality code.  This is for fun, and I'm *sure* my classes could be factored in a much cleaner way.

# Notes
## Iterative Deepening
* [Chess Programming Wiki - Iterative Deepening](https://www.chessprogramming.org/Iterative_Deepening)
* [Iterative Deepening Search(IDS) or Iterative Deepening Depth First Search(IDDFS)](https://www.geeksforgeeks.org/iterative-deepening-searchids-iterative-deepening-depth-first-searchiddfs/)
* [Iterative deepening depth-first search](https://en.wikipedia.org/wiki/Iterative_deepening_depth-first_search)
* 