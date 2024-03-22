# Tournament Testing
I am using cutechess-cli for tournament testing.  Below are notes for me to rerun my tournaments.

Note: my [debugging guide](debugging.md) for test and debugging workflows.

## Prepare cutechess
To run on MacOS, follow [compilation guide on the cutechess wiki](https://github.com/cutechess/cutechess/wiki/Building-from-source#macos).  For ease of execution, also run `make install`.

## Running a tournament

```bash
export MARKS_CHESS_DIR=$HOME/IdeaProjects/marks-chess 
cd $MARKS_CHESS_DIR
```

### Tournament example
```bash
cutechess-cli \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100" dir=$MARKS_CHESS_DIR cmd=./bin/app.sh  name="ABTTdinf" \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4" dir=$MARKS_CHESS_DIR cmd=./bin/app.sh  name="ABTTd4" \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 2 -bd 2" dir=$MARKS_CHESS_DIR cmd=./bin/app.sh  name="ABTTd2" \
  -engine arg="-w AlphaBetaPlayer -b AlphaBetaPlayer -wd 2 -bd 2" dir=$MARKS_CHESS_DIR cmd=./bin/app.sh  name="ABd2" \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/15 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 10 \
  > $MARKS_CHESS_DIR/log/output_engine.log 2>&1
```

Explaination:
* have stockfish run at about 1350 ELO -> option.UCI_LimitStrength=true "option.Use NNUE=false"

### Tournament with different evaluators
```bash
cutechess-cli \
  -engine cmd=./bin/app.sh  name="ABTTd4-se"  arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4 -e SimpleEvaluator"  dir=$MARKS_CHESS_DIR \
  -engine cmd=./bin/app.sh  name="ABTTd4-cai" arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4 -e ChessAIEvaluator" dir=$MARKS_CHESS_DIR \
  -engine cmd=./bin/app.sh  name="ABTTd4-p"   arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4 -e PestoEvaluator"   dir=$MARKS_CHESS_DIR \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/15 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 30 \
  > $MARKS_CHESS_DIR/log/output_engine_evaluators.log 2>&1
```

### Using SPRT instead of fixed games
```bash
cutechess-cli \
  -engine cmd=./bin/app.sh  name="ABTTd4-se"  arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4 -e SimpleEvaluator"  dir=$MARKS_CHESS_DIR \
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


## Evalating Elo
```
% ./bin/bayeselo
version 0057, Copyright (C) 1997-2010 Remi Coulom.
compiled Mar  2 2024 15:36:58.
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under the terms and conditions of the GNU General Public License.
See http://www.gnu.org/copyleft/gpl.html for details.
ResultSet>readpgn log/output_pgn_file.pgn
1100 game(s) loaded, 0 game(s) with unknown result ignored.
ResultSet>elo
ResultSet-EloRating>mm
00:00:00,00
ResultSet-EloRating>exactdist
00:00:00,00
ResultSet-EloRating>ratings
Rank Name             Elo    +    - games score oppo. draws
   1 Stockfish 15.1   108   11   11  1100   74%  -108    4%
   2 ABTTd4          -108   11   11  1100   26%   108    4%
```

# History of Results
## Version 1 - 20240317
[git sha 8f50187](https://github.com/markmansour/marks-chess/tree/8f50187)
```bash
cutechess-cli \
  -engine name="ABTTdinf" cmd=./bin/app.sh arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100" dir=$MARKS_CHESS_DIR \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/60 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 50 \
  -debug \
  > $MARKS_CHESS_DIR/log/output_engine.log 2>&1
```

### From cutechess
```
Finished game 100 (Stockfish 15.1 vs ABTTdinf): 0-1 {Black mates}
Score of ABTTdinf vs Stockfish 15.1: 41 - 58 - 1  [0.415] 100
...      ABTTdinf playing White: 27 - 23 - 0  [0.540] 50
...      ABTTdinf playing Black: 14 - 35 - 1  [0.290] 50
...      White vs Black: 62 - 37 - 1  [0.625] 100
Elo difference: -59.6 +/- 69.6, LOS: 4.4 %, DrawRatio: 1.0 %
SPRT: llr 0 (0.0%), lbound -inf, ubound inf

Player: ABTTdinf
   "Draw by 3-fold repetition": 1
   "Loss: Black disconnects": 2
   "Loss: Black mates": 22
   "Loss: White disconnects": 1
   "Loss: White mates": 33
   "Win: Black loses on time": 2
   "Win: Black mates": 14
   "Win: White mates": 25
Player: Stockfish 15.1
   "Draw by 3-fold repetition": 1
   "Loss: Black loses on time": 2
   "Loss: Black mates": 14
   "Loss: White mates": 25
   "Win: Black disconnects": 2
   "Win: Black mates": 22
   "Win: White disconnects": 1
   "Win: White mates": 33
Finished match
```

### bayeselo Elo
```
Rank Name             Elo    +    - games score oppo. draws
  1 Stockfish 15.1    36   33   32   100   59%   -36    1%
  2 ABTTdinf         -36   32   33   100   42%    36    1%
```


