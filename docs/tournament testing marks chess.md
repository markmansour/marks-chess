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
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100" dir=$MARKS_CHESS_DIR cmd=./app.sh  name="ABTTdinf" \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4" dir=$MARKS_CHESS_DIR cmd=./app.sh  name="ABTTd4" \
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 2 -bd 2" dir=$MARKS_CHESS_DIR cmd=./app.sh  name="ABTTd2" \
  -engine arg="-w AlphaBetaPlayer -b AlphaBetaPlayer -wd 2 -bd 2" dir=$MARKS_CHESS_DIR cmd=./app.sh  name="ABd2" \
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
## Version 1 - 20240309
[git sha 8f50187](https://github.com/markmansour/marks-chess/tree/8f50187)
```bash
cutechess-cli \                                                       ✔  10s   base   2.0.0-p247   09:46:33 
  -engine arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4" dir=$MARKS_CHESS_DIR cmd=./bin/app.sh  name="ABTTdinf" \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/15 \
  -pgnout $MARKS_CHESS_DIR/log/output_pgn_file.pgn \
  -recover \
  -concurrency 8 \
  -games 2 \
  -rounds 250 \
  > $MARKS_CHESS_DIR/log/output_engine.log 2>&1
```

From cutechess
```
Score of ABTTdinf vs Stockfish 15.1: 114 - 364 - 22  [0.250] 500
```

bayeselo Elo
```
Rank Name             Elo    +    - games score oppo. draws
   1 Stockfish 15.1   121   15   15   634   77%  -121    4%
   2 ABTTdinf        -121   15   15   634   23%   121    4%
```


