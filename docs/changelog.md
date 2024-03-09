# Changelog

## Pre v1. 20240303
Estimated Elo Rating: ~1200

Command
```bash
cutechess-cli \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4" dir=$MARKS_CHESS_DIR cmd=./app.sh  name="ABTTd4" \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/15 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 500 \
> $MARKS_CHESS_DIR/log/output_engine.log 2>&1
```

Results:

Win Ratio (231 + (39/2))/1000 => 25%

#### Score of ABTTd4 vs Stockfish 15.1: 231 - 730 - 39

```
Finished game 1000 (Stockfish 15.1 vs ABTTd4): 0-1 {Black mates}
Score of ABTTd4 vs Stockfish 15.1: 231 - 730 - 39  [0.251] 1000
...      ABTTd4 playing White: 118 - 362 - 20  [0.256] 500
...      ABTTd4 playing Black: 113 - 368 - 19  [0.245] 500
...      White vs Black: 486 - 475 - 39  [0.505] 1000
Elo difference: -190.4 +/- 24.2, LOS: 0.0 %, DrawRatio: 3.9 %
SPRT: llr 0 (0.0%), lbound -inf, ubound inf

Player: ABTTd4
"Draw by 3-fold repetition": 28
"Draw by insufficient mating material": 2
"Draw by stalemate": 9
"Loss: Black disconnects": 8
"Loss: Black loses on time": 11
"Loss: Black makes an illegal move: e4d3": 1
"Loss: Black makes an illegal move: e8c8": 6
"Loss: Black mates": 340
"Loss: White disconnects": 7
"Loss: White loses on time": 12
"Loss: White makes an illegal move: d5c6": 1
"Loss: White makes an illegal move: d5e6": 2
"Loss: White mates": 342
"Win: Black mates": 113
"Win: White mates": 118
Player: Stockfish 15.1
"Draw by 3-fold repetition": 28
"Draw by insufficient mating material": 2
"Draw by stalemate": 9
"Loss: Black mates": 113
"Loss: White mates": 118
"Win: Black disconnects": 8
"Win: Black loses on time": 11
"Win: Black makes an illegal move: e4d3": 1
"Win: Black makes an illegal move: e8c8": 6
"Win: Black mates": 340
"Win: White disconnects": 7
"Win: White loses on time": 12
"Win: White makes an illegal move: d5c6": 1
"Win: White makes an illegal move: d5e6": 2
"Win: White mates": 342
```

