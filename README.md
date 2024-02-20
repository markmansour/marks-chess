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
* Perft 7 running (correctly) at 4MM nodes/second on my M2 macbook air.  Perft 7 for Java requires moving 
  from ints to longs (64 bit) to support the higher number of nodes.
* Four working players.  
  * RandomMovePlayer - generates moves to a depth of 1 and randomly picks a move.
  * BasicNegaMaxPlayer - uses nagamax (minimax) with a [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) 
    (values in centipawns) and [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables).
  * AlphaBetaPlayer with alpha/beta pruning (Simple evaluation and PSTs).
  * AlphaBetaPlayerTT with alpha/beta and transposition tables.
* Move ordering (basic version implemented to prioritize captures) and improved move ordering ([MVV-LVA](https://www.chessprogramming.org/MVV-LVA)).
* Zobrist keys to find repetition and support speed improvements during search.

## Design
* **Game** - contains only game state such as clock moves, draw/checked/mate/repetition/50 move rule, the active player, and a history of moves.  Players make moves in the game object, which updates the underlying board.  The Game object deals in moves including move generation and enforces correctness of moves.
* **Board** - this class represents the board layout including where pieces are placed, and hashes of the board state.  It does not enforce correctness.
* **Pieces** - com.stateofflux.chess.model.pieces.*: This package attempts to encapsulate all piece logic.
* **Players** - logic to determine how to move pieces on the board.  Includes search and evaluation functions.  In traditional chess engines this is called artificial intelligence.

## Build
Built against [Amazon Corretto JDK 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/what-is-corretto-17.html) and Apache Maven 3.9+.

If you're building the project on the command line then to get started:

```bash
$ git clone https://github.com/markmansour/marks-chess.git
$ cd marks-chess
$ mvn package assembly:single
```

## Playing against other engines
Build a single jar file with all dependencies, and call it from a shell script.  That
shell script can be called from a chess engine.

```bash
$ ~./app.sh
```

I use this script as a launch for GUIs such as [cutechess](https://github.com/cutechess/cutechess) or banksia on macOS.

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
The below notes are mainly for me, but I'll leave them in as they may help others.

## Suggestions the features to build and the order to build them
* [Suggested ordering for improving chess engine](https://www.reddit.com/r/chessprogramming/comments/mctk27/what_are_the_lowest_hanging_fruits_for_greater/)
* [another suggestion](https://www.reddit.com/r/chessprogramming/comments/fxiz8u/how_much_faster_are_bitboards_as_opposed_to_a_2d/).
* [Comparative Advantage of Engine Improvements](https://www.reddit.com/r/ComputerChess/comments/yln9ef/comparative_advantage_of_engine_improvements/) (reddit)

## Overview of search
* Alpha Beta worked example video [Algorithms Explained – minimax and alpha-beta pruning](https://www.youtube.com/watch?v=l-hh51ncgDI) by Sebastian Lague
* [Alpha-Beta with Sibling Prediction Pruning in Chess - Jeroen W.T. Carolus](https://homepages.cwi.nl/%7Epaulk/theses/Carolus.pdf)
* [A review of game-tree pruning - T.A. Marsland](https://webdocs.cs.ualberta.ca/%7Etony/OldPapers/icca.Mar1986.pp3-18.pdf)

## Transposition Tables
* [Wikipedia](https://en.wikipedia.org/wiki/Transposition_table) and [Chessprogramming wiki](https://www.chessprogramming.org/Transposition_Table) (very general) 
* Implementations: [blunder](https://github.com/algerbrex/blunder/blob/main/engine/transposition.go), [kengine](https://github.com/bhlangonijr/kengine/blob/main/src/main/java/com/github/bhlangonijr/kengine/alphabeta/TranspositionTable.kt),  

## Iterative Deepening
* [Chess Programming Wiki - Iterative Deepening](https://www.chessprogramming.org/Iterative_Deepening)
* [Iterative Deepening Search(IDS) or Iterative Deepening Depth First Search(IDDFS)](https://www.geeksforgeeks.org/iterative-deepening-searchids-iterative-deepening-depth-first-searchiddfs/)
* [Iterative deepening depth-first search](https://en.wikipedia.org/wiki/Iterative_deepening_depth-first_search)

# Useful Tools
* [Lichess Board Editor](https://lichess.org/editor) (great for creating testing scenarios)

## Implementations
* [Shallow-Blue](https://github.com/GunshipPenguin/shallow-blue) (Java)
* [Chess AI](https://github.com/zeyu2001/chess-ai/tree/main) (JS)
* [chesslib](https://github.com/bhlangonijr/chesslib) (Java)
* [chess.js](https://github.com/jhlywa/chess.js/tree/master) (JavaScript)
* [Stockfish](https://github.com/official-stockfish/Stockfish/tree/master) (C++)
* [Leorik](https://github.com/lithander/Leorik/tree/master) (C#)

To look at
* [Sunfish](https://github.com/thomasahle/sunfish/tree/master)
* [Mediocre Chess](https://mediocrechess.sourceforge.net/guides/see.html) - [transposition tables](http://mediocrechess.blogspot.com/2007/01/guide-transposition-tables.html)
* [kengine](https://github.com/bhlangonijr/kengine/tree/main)
* [Rustic Chess Engine](https://rustic-chess.org/evaluation/psqt.html) docs and [code](https://github.com/mvanthoor/rustic)

Polyglot readers
* [jchesslib](https://github.com/asdfjkl/jchesslib/tree/main) for Java Polyglot implementation
* [chesstango](https://github.com/mcoria/chesstango/tree/master/engine/src/main/java/net/chesstango/engine/polyglot)
* [carballo](https://github.com/albertoruibal/carballo/blob/9d0e08d5d7f6869f05cf986aed263f44d36bf6af/jse/src/main/java/com/alonsoruibal/chess/book/FileBook.java)

[Coding Adventure: Making a Better Chess Bot](https://www.youtube.com/watch?v=_vqlIPDR2TU) by Sebastian Lague

# Potential Future Capabilities
* Improved search (Iterative deepening, widening aspiration windows, quiescence, Transposition Tables (done))
* Pruning and Reduction (late move reduction, null moves, futility pruning)
* Improved evaluation (king attacked)
* Opening books (See [lichess openings](https://github.com/lichess-org/chess-openings/tree/master)), [BALSA](https://sites.google.com/site/computerschess/balsa-opening-test-suite), [beginners guide](http://horizonchess.com/FAQ/Winboard/openingbook.html), [Polyglot book format](http://hgm.nubati.net/book_format.html), [Chess Stack Exchange](https://chess.stackexchange.com/questions/5933/how-to-create-your-own-opening-book-for-your-own-chess-engine) thread, [Bluefever software video](https://www.youtube.com/watch?v=hGy5kR_mOdM)
* End games
* Calculate the rough [Elo](https://en.wikipedia.org/wiki/Elo_rating_system) for my engine.  Tools to review: [Openbench](https://github.com/AndyGrant/OpenBench), [Chess Tuning Tool](https://chess-tuning-tools.readthedocs.io/en/latest/index.html), and CuteChess for tournament simulation.  [Tutorial from lc0](https://lczero.org/dev/wiki/testing-guide/) may be of help.
* Compare my chess engine against other engines using [Computer Chess Rating Lists](http://computerchess.org.uk/ccrl/).  Currently trying to beat "[Dumb](https://github.com/abulmo/Dumb/tree/master)" but not succeeding as it's Elo rating is 2698 (my bot isn't close to this (yet!))
* Threaded search and evaluation
* Time management
* Neural Net chess player - see [Minic](https://github.com/tryingsomestuff/Minic), [maia](https://github.com/CSSLab/maia-chess), [sunfish](https://github.com/thomasahle/sunfish), [Bagatur](https://github.com/bagaturchess/Bagatur) (Java).  Perhaps [bullet](https://github.com/jw1912/bullet) would be useful for training?
* Resignations

## Neural Net Research
* [Stockfish NNUE doc explaining how NNUEs work](https://github.com/official-stockfish/nnue-pytorch/blob/master/docs/nnue.md)
* [Neural Networks for chess](https://github.com/asdfjkl/neural_network_chess) by Dominik Klein
* [Coding Your First Neural Network FROM SCRATCH](https://code.likeagirl.io/coding-your-first-neural-network-from-scratch-0b28646b4043)
* [DeepLearning4J](https://deeplearning4j.konduit.ai) - with links to learning resources

### Features (in priority order)
1. [x] verify alpha/beta search is working (seems to have a bug)
2. [x] iterative deepening
3. [x] timeouts
4. [ ] integration with other bots - get a baseline ELO
5. [ ] create basic neural net player

### Papercuts to address (in priority order)
1. [ ] add copyright notice to files
2. [ ] add stylecheck into build process
3. [ ] improve coverage (95%+)
4. [ ] is there anything I've learned from this experience that I'd like to write up and share (blog)
5. [ ] I don't love my logging patterns.  Review and improve.
6. [ ] Improve speed of perft (Board functions that iterate through all boards to find pieces)
