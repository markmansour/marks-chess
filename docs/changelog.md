# Changelog


## Unreleased

Search
* The search is fully deterministic: the principal variation, transposition-table entries, and node
  counts are reproducible. Only the move actually played is randomised, chosen among equal-scoring
  root moves after the search so it cannot affect the result.

Performance testing
* Added an A/B benchmark and a deterministic node-count regression gate for hash-move ordering; the
  gate runs in the normal test suite, so every PR is protected.
* Added a perft move-generation throughput benchmark, tracked over time in CI with
  github-action-benchmark. See [performance testing](performance-testing.md).

Build & CI
* Fixed the CI build, which failed on the deprecated `actions/upload-artifact@v2`.
* Replaced the self-committing JaCoCo coverage badge with Codecov, so CI no longer commits to `main`
  on every run.

## v1.1 20260621

Bugs
* 50 move rule accounts for moves from both players.
* PGN parser no longer loses moves due to greedy commenting parsing.
* PeSTO evaluator scores correctly. Its piece-value, game-phase and board-orientation
  tables were misaligned with the engine's piece indexing, so the start position scored
  -398 instead of 0.
* The black player is built with the black colour (it was constructed as white).
* FEN move counters round-trip. The half-move clock and full-move counter are stored
  independently, seeded from the loaded FEN, and restored on undo, so loading and
  re-emitting a FEN preserves both counters and the 50-move rule reflects the loaded clock.
* Zobrist keys give the piece-square, castling, en-passant and side-to-move categories
  disjoint index ranges, eliminating key collisions that could produce false
  threefold-repetition draws.

Search
* Checkmate and stalemate are scored at terminal nodes, so the engine reliably finds
  forced mate (preferring shorter mates) and treats stalemate as a draw.
* The transposition table's stored best move is searched first (hash-move ordering) and is
  validated against the legal moves, so a colliding entry can never surface an illegal move.
* Transposition table mate scores are corrected by the search distance from the root rather
  than the absolute game move number, keeping stored mate scores valid across searches.
* Move tie-breaks are randomised only at the root; interior nodes are deterministic, making
  the principal variation reproducible.

Evaluation
* End-game detection is computed per position instead of latching a flag and mutating shared
  state, so reaching an end game in one search line no longer corrupts the evaluation of
  later, piece-rich positions (or the opponent, who shares the evaluator).

## v1. 20240322
Chess Engine / Chess move generator written in Java.  

Engine:
* Bitboards to represent the board
* Magic bitboard move generator
* Basic UCI protocol

Search
* Alpha/Beta search (negascout) and iterative deepening
* Transposition Tables
* MVV-LVA move ordering

Evaluation
* Material counting
* Piece-Square Tables

#### Elo calculation is as follows:
WIP: Feedback welcomed.

Assuming Stockfish 15 Elo with limited strength has an Elo of 1350, and I use SPRT to evaluate whether I'm within
50 Elo (not stronger), then Mark's Chess it is at least 50 Elo weaker than Stockfish.

```bash
cutechess-cli \
  -engine cmd=./bin/app.sh  name="ABTTd4-se"  arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100 -e SimpleEvaluator"  dir=$MARKS_CHESS_DIR \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci \
    tc=40/15 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -sprt elo0=0 elo1=50 alpha=0.05 beta=0.05 \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 2500 \
  -repeat 2 \
  -maxmoves 200 \
  -ratinginterval 10 \
  > $MARKS_CHESS_DIR/log/output_engine_evaluators.log 2>&1
```

```text
Finished game 97 (ABTTd4-se vs Stockfish 15.1): * {No result}
Score of ABTTd4-se vs Stockfish 15.1: 38 - 51 - 5  [0.431] 94
    ...      ABTTd4-se playing White: 20 - 24 - 3  [0.457] 47
    ...      ABTTd4-se playing Black: 18 - 27 - 2  [0.404] 47
    ...      White vs Black: 47 - 42 - 5  [0.527] 94
Elo difference: -48.4 +/- 69.8, LOS: 8.4 %, DrawRatio: 5.3 %
SPRT: llr -3.03 (-103.0%), lbound -2.94, ubound 2.94 - H0 was accepted
```
