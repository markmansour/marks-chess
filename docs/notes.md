The below notes are mainly for me, but I'll leave them in as they may help others.

## Suggestions the features to build and the order to build them
* [Suggested ordering for improving chess engine](https://www.reddit.com/r/chessprogramming/comments/mctk27/what_are_the_lowest_hanging_fruits_for_greater/)
* [another suggestion](https://www.reddit.com/r/chessprogramming/comments/fxiz8u/how_much_faster_are_bitboards_as_opposed_to_a_2d/).
* [Comparative Advantage of Engine Improvements](https://www.reddit.com/r/ComputerChess/comments/yln9ef/comparative_advantage_of_engine_improvements/) (reddit)

## Speed
* Perft 7 running at 4MM nodes/second on my M2 macbook air (in comparison to 36MM from
  [QBB Perft](https://github.com/lithander/QBB-Perft/tree/master) a highly optimized and specific
  implementation).

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
* [Shallow-Blue](https://github.com/GunshipPenguin/shallow-blue) (C++)
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
* Improved search (~~Iterative deepening~~, widening aspiration windows, quiescence, ~~Transposition Tables~~)
* Pruning and Reduction (late move reduction, null moves, futility pruning)
* Improved evaluation (king attacked)
* Opening books (See [lichess openings](https://github.com/lichess-org/chess-openings/tree/master)), [BALSA](https://sites.google.com/site/computerschess/balsa-opening-test-suite), [beginners guide](http://horizonchess.com/FAQ/Winboard/openingbook.html), [Polyglot book format](http://hgm.nubati.net/book_format.html), [Chess Stack Exchange](https://chess.stackexchange.com/questions/5933/how-to-create-your-own-opening-book-for-your-own-chess-engine) thread, [Bluefever software video](https://www.youtube.com/watch?v=hGy5kR_mOdM)
* End games
* Calculate the rough [Elo](https://en.wikipedia.org/wiki/Elo_rating_system) for my engine.  Tools to review: [Baysian Elo](https://www.remi-coulom.fr/Bayesian-Elo/), [Openbench](https://github.com/AndyGrant/OpenBench), [Chess Tuning Tool](https://chess-tuning-tools.readthedocs.io/en/latest/index.html), and CuteChess for tournament simulation.  [Tutorial from lc0](https://lczero.org/dev/wiki/testing-guide/) may be of help.
* Compare my chess engine against other engines using [Computer Chess Rating Lists](http://computerchess.org.uk/ccrl/).  Currently trying to beat "[Dumb](https://github.com/abulmo/Dumb/tree/master)" but not succeeding as it's Elo rating is 2698 (my bot isn't close to this (yet!))
* Threaded search and evaluation
* Time management (very basic version done)
* Neural Net chess player - see [Minic](https://github.com/tryingsomestuff/Minic), [maia](https://github.com/CSSLab/maia-chess), [sunfish](https://github.com/thomasahle/sunfish), [Bagatur](https://github.com/bagaturchess/Bagatur) (Java).  Perhaps [bullet](https://github.com/jw1912/bullet) would be useful for training?
* Resignations

## Neural Net Research
* [Stockfish NNUE doc explaining how NNUEs work](https://github.com/official-stockfish/nnue-pytorch/blob/master/docs/nnue.md)
* [Neural Networks for chess](https://github.com/asdfjkl/neural_network_chess) by Dominik Klein
* [Coding Your First Neural Network FROM SCRATCH](https://code.likeagirl.io/coding-your-first-neural-network-from-scratch-0b28646b4043)
* [DeepLearning4J](https://deeplearning4j.konduit.ai) - with links to learning resources

## References
* [Practical Artificial Intelligence](https://leanpub.com/javaai/read#search)
* [Artificial Intelligence: A Modern Approach](https://aima.cs.berkeley.edu) [github](https://github.com/aimacode/aima-java) - Search, NN, deep learning.  See Chapter 6.3 for Alpha-Beta trees, Late Move Reduction

### Features (in priority order)
* [x] verify alpha/beta search is working (seems to have a bug)
* [x] iterative deepening
* [x] timeouts
* [x] integration with other bots - get a baseline ELO
* [ ] create basic neural net player

### Bugs
* [x] AlphaBetaWithTT occasionally generates illegal PV.

### Papercuts to address (in priority order)
* [ ] Improve speed of evaluation function as it's very slow.
* [ ] add copyright notice to files
* [ ] add stylecheck into build process
* [ ] add dependency management checks ([dependabot](https://github.com/dependabot/dependabot-core)?) or ([renovatebot](https://docs.renovatebot.com/java/#maven-file-support)) to build process
* [ ] improve coverage (95%+)
* [ ] is there anything I've learned from this experience that I'd like to write up and share (blog)
* [ ] I don't love my logging patterns.  Review and improve.
* [ ] Improve speed of perft (Board functions that iterate through all boards to find pieces)
* [ ] En passant logic needs a rewrite/cleanup.
