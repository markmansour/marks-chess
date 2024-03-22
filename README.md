# Mark's Chess
[![CI workflow](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml/badge.svg)](https://github.com/markmansour/marks-chess/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/markmansour/marks-chess/actions/workflows/build.yml)

This is a straight forward chess engine written in Java.  The chess engine comes with a basic UCI interface.
There are thorough unit tests.  There is *no* GUI.

## Chess Engine Capabilities
The project contains a chess engine and a few AI players.  See [Release Notes](docs/changelog.md) for more details.

### Chess Engine
* Understands all valid moves including castling, en passant, and promotion.
* Understands checkmate, stalemate, the 50 rule move, insufficient materials, and repetition.
* Basic UCI interface.
* Uses bitboards (and
  [Magic bitboards](https://rhysre.net/fast-chess-move-generation-with-magic-bitboards.html)) for speed.
* Perft 7 running at 4MM nodes/second on my M2 MacBook Air running in a single thread.
 
### Automated Players
The main player AI is [AlphaBetaPlayerTT](src/main/java/com/stateofflux/chess/model/player/AlphaBetaPlayerWithTT.java).
It has the following features:
* [Simple Evaluation Function](https://www.chessprogramming.org/Simplified_Evaluation_Function) utilizing
  [Piece-Square Tables](https://www.chessprogramming.org/Simplified_Evaluation_Function#Piece-Square_Tables) and board
  material.
* Move ordering (basic version implemented to prioritize captures) and improved move ordering
  ([MVV-LVA](https://www.chessprogramming.org/MVV-LVA)).
* Incrementally updated [Zobrist hashing](https://www.chessprogramming.org/Zobrist_Hashing) to find repetition and support speed improvements during search.
* [Iterative Deepening](https://www.chessprogramming.org/Iterative_Deepening) and basic [Time management](https://www.chessprogramming.org/Time_Management).

Other players include:
  * [RandomMovePlayer](src/main/java/com/stateofflux/chess/model/player/RandomMovePlayer.java) - generates
    moves to a depth of 1 and randomly picks a move.
  * [BasicNegaMaxPlayer](src/main/java/com/stateofflux/chess/model/player/BasicNegaMaxPlayer.java) - uses
    negamax (minimax).
  * [AlphaBetaPlayer](src/main/java/com/stateofflux/chess/model/player/AlphaBetaPlayer.java) with alpha/beta
    pruning (Simple evaluation and PSTs).  No iterative deepening or time management.
  * [Pesto](src/main/java/com/stateofflux/chess/model/player/PestoEvaluator.java), 
    [ChessAI](src/main/java/com/stateofflux/chess/model/player/ChessAIEvaluator.java), 
    [Material](src/main/java/com/stateofflux/chess/model/player/MaterialEvaluator.java) evaluators that I used as 
    I built up my and debugged my Simple Evaluator.

## Design
High level class design:
* **Game** - contains only game state such as clock moves, draw/checked/mate/repetition/50 move rule, the
  active player, and a history of moves.  Players make moves in the game object, which updates the underlying
  board.  The Game object deals in moves including move generation and enforces correctness of moves.
* **Board** - this class represents the board layout including where pieces are placed, and hashes of the
  board state.  It does not enforce correctness.
* **Pieces** - com.stateofflux.chess.model.pieces.*: This package attempts to encapsulate all piece logic instead of 
  placing it throughout the Board and Game code.
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

## Utilizing the chess engine in your own code
To generate legal moves - here is my unit test code:
```java
@Test public void queenSideBlackCastlingWhenBlocked() {
    Game game = new Game("rn2kbnr/ppp1pppp/3q4/3p4/8/P6N/RPPPPPPP/1NBQKB1R w Kkq -");
    MoveList<Move> moves = game.generateMoves();
    assertThat(moves.asLongSan()).doesNotContain("e1c8");  // white king to black rook with blockers
    assertThat(moves).hasSize(19);
}
```

To generate pseudo legal moves:
```java
@Test public void removeIllegalMoves() {
    Game game = new Game("r3kbnr/pp3ppp/n7/2pqP1B1/6b1/4P3/PP3PPP/RN1QKBNR b KQkq -");
    MoveList<Move> generatedMoves;

    generatedMoves = game.pseudoLegalMoves();
    assertThat(generatedMoves).hasSize(47);
    assertThat(generatedMoves.asLongSan()).contains("e8e7");  // move into check
    generatedMoves = game.generateMoves();
    assertThat(generatedMoves).hasSize(45);
    assertThat(generatedMoves.asLongSan()).doesNotContain("e8e7");
}
```

To evaluate a position
```java
    Game game = new Game("2kr2nr/pp3ppp/2p5/2P1nb2/4q3/7P/PPP1BPP1/R1BQK1NR b - - 0 16");
    Evaluator evaluator = new SimpleEvaluator();
    AlphaBetaPlayerWithTT black = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, evaluator);
    
    // terminate at either a depth of 200 or after 5 seconds.
    black.setIncrement(TimeUnit.SECONDS.toNanos(5));
    black.setSearchDepth(200);

    Move bestMove = black.getNextMove(game);
```

To run a full game:
```java
    Game game = new Game();
    
    Evaluator simpleEvaluator = new SimpleEvaluator();
    AlphaBetaPlayerWithTT one = new AlphaBetaPlayerWithTT(PlayerColor.WHITE, simpleEvaluator);
    one.setSearchDepth(100);
    one.setIncrement(TimeUnit.SECONDS.toNanos(2));
    
    Evaluator pestoEvaluator = new PestoEvaluator();
    AlphaBetaPlayerWithTT two = new AlphaBetaPlayerWithTT(PlayerColor.BLACK, pestoEvaluator);
    two.setSearchDepth(100);
    two.setIncrement(TimeUnit.SECONDS.toNanos(2));
    
    game.play(one, two);
    game.printOccupied();
```

The unit tests have many useful examples.

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
And there are 1000s of chess engines in existence, so this isn't about invention.  It's about me having fun, having
an algorithmically interesting challenge, and problem that has many levels so I can achieve wins quickly but also 
continue to add features and enhancements on a regular basis and in an incremental way.

## Goals
* Have fun!  It's been a while since I've regularly programmed.  This project is my way of re-engaging with
  programming.  
* Re-familiarize myself with the Java ecosystem.  I wanted to better understand the modern JDKs, testing frameworks, and 
  overall ecosystem (including AssertJ, JUnit, TestNG, Google Guava, SpotBugs, Maven, Code Coveraget tools, etc.).
* Experiment with benchmarking frameworks (using [async profiler](https://github.com/async-profiler/async-profiler)).
* Experiment with 3rd party services (such as GitHub actions)
* Move from theory to practice with Neural Nets.

## Non-goals
* Make a competitive chess engine.  If it happens, then that's a nice (but unexpected) bonus.
* Write production quality code.  This is for fun, and I'm *know* my classes could be factored in a much
  cleaner way.
