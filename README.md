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
* Two working players.  
  * RandomMovePlayer - generates moves to a depth of 1 and randomly picks a move.
  * BasicNegaMaxPlayer - uses nagamax (minimax) with a [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) 
    (values in centipawns) and [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables).


### Next steps
* Opening books (See [lichess openings](https://github.com/lichess-org/chess-openings/tree/master)), [BALSA](https://sites.google.com/site/computerschess/balsa-opening-test-suite), [beginners guide](http://horizonchess.com/FAQ/Winboard/openingbook.html), [Polyglot book format](http://hgm.nubati.net/book_format.html), [Chess Stack Exchange](https://chess.stackexchange.com/questions/5933/how-to-create-your-own-opening-book-for-your-own-chess-engine) thread, [Bluefever software video](https://www.youtube.com/watch?v=hGy5kR_mOdM)   
* End games
* Calculate the rough [Elo](https://en.wikipedia.org/wiki/Elo_rating_system) for my engine
* Compare my chess engine against other engines using [Computer Chess Rating Lists](http://computerchess.org.uk/ccrl/).  Currently trying to beat "[Dumb](https://github.com/abulmo/Dumb/tree/master)" but not succeeding as it's Elo rating is 2698 (my bot isn't close to this (yet!)).
* Player with alpha/beta pruning
* Move ordering
* Multi-threading
* Time management
* AI chess player (neural nets) - see [maia](https://github.com/CSSLab/maia-chess), [sunfish](https://github.com/thomasahle/sunfish), 
* Resignations

### Maybes
* Monte Carlo Tree search
* Tournament Framework

## Build
IntelliJ project included.  

If you're doing this on the command line then to get started:

```bash
$ git clone https://github.com/markmansour/marks-chess.git
$ cd marks-chess
$ mvn package
```

## Goals
* Have fun!  It's been years since I've regularly programmed and this project is my way of re-engaging with programming.  And there are 1000s of chess engines in existence so I have absolutely no intention of commercializing this code.
* Refamiliarize myself with the Java ecosystem (including AssertJ, JUnit, TestNG, Google Guava, SpotBugs, Maven, etc).
* Experiment with benchmarking frameworks (using [async profiler](https://github.com/async-profiler/async-profiler)).
* Experiment with 3rd party services (such as github actions)
* Move from theory to practice with Neural Nets.

## Non-goals
* Make a competitive chess engine.  If it happens, then that's a nice (but unexpected) bonus.
* Write production quality code.  This is for fun and I'm *sure* my classes could be factored in a much cleaner way.

## Playing against other engines
Build a single jar file with all dependencies, and call it from a shell script.  That
shell script can be called from a chess engine.

Create a package:
```bash
$ mvn package assembly:single
```

Create a OS launcher (shell script) containing..
```bash
/bin/bash -c 'cd <PATH>/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App'
```
You'll need to make it executable (chmod +x <shell script>).

I've added marks-chess to [cutechess](https://github.com/cutechess/cutechess) on MacOS and had it compete against itself, a human, and stockfish.


