# Changelog


# v1.1 20240323

Added basic quiescence search.  If at depth 0 and the last move was a capture, then search for up to 2 additional 
levels of depth to see if the results is unfavorable.  Only review other captures.

```bash
cutechess-cli \
  -engine cmd=./bin/app.sh  name="AB-v1"    arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100 -e SimpleEvaluator"                      dir=$MARKS_CHESS_DIR/../marks-chess-v1/ \
  -engine cmd=./bin/app.sh  name="AB-v1.1"  arg="-w AlphaBetaPlayerWithTTQuiescence -b AlphaBetaPlayerWithTTQuiescence -wd 100 -bd 100 -e SimpleEvaluator"  dir=$MARKS_CHESS_DIR \
  -each proto=uci \
    tc=40/60 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -sprt elo0=0 elo1=10 alpha=0.05 beta=0.05 \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 2500 \
  -repeat 2 \
  -maxmoves 200 \
  -ratinginterval 10 \
  -debug \
  > $MARKS_CHESS_DIR/log/output_engine.log 2>&1
```

From cutechess:
```text
Score of AB-v1 vs AB-v1.1: 34 - 44 - 17       [0.447] 95
...      AB-v1 playing White:   14 - 21 - 12  [0.426] 47
...      AB-v1 playing Black:   20 - 23 - 5   [0.469] 48
...      White vs Black:        37 - 41 - 17  [0.479] 95
Elo difference: -36.7 +/- 64.2, LOS: 12.9 %, DrawRatio: 17.9 %
SPRT: llr -2.98 (-101.1%), lbound -2.94, ubound 2.94 - H0 was accepted

```
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
  -engine cmd=./bin/app.sh  name="AB-v1"  arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100 -e SimpleEvaluator"  dir=$MARKS_CHESS_DIR \
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
Score of ABTTd4-se vs Stockfish 15.1: 38 - 51 - 5  [0.431] 94
    ...      AB-v1 playing White:     20 - 24 - 3  [0.457] 47
    ...      AB-v1 playing Black:     18 - 27 - 2  [0.404] 47
    ...      White vs Black:          47 - 42 - 5  [0.527] 94
Elo difference: -48.4 +/- 69.8, LOS: 8.4 %, DrawRatio: 5.3 %
SPRT: llr -3.03 (-103.0%), lbound -2.94, ubound 2.94 - H0 was accepted
```
