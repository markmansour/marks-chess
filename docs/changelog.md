# Changelog

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
