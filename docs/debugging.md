# Debugging
Debugging is a variation on [tournament testing](tournament%20testing%20marks%20chess.md).

#### Evaluator and Alpha-Beta debugging
Requirements: 
* Install [`cutechess`](https://github.com/cutechess/cutechess).  See [MacOS specific build instructions](https://github.com/cutechess/cutechess/wiki/Building-from-source#macos).
* Install xmlstarlet  # brew install xmlstarlet

1. change the level from info to debug in `logback.xml`.  While you're there, make sure the logfile is going to the right directory.

```xml
<logger name="com.stateofflux.chess.alpha-beta-debugging" level="debug">
   <appender-ref ref="alpha-beta-file-debugger" />
</logger>
```

2. Remove the old log files:
Note that `rm` with a wildcard in zsh in interactive mode always prompts the user unless you set rm_star_silent.
```bash
setopt rm_star_silent
```
```bash
rm -f log/*
```

3. Rebuild the package
```bash
mvn package assembly:single
```
4. Run cutechess-cli for a single game:
[Computer Chess Rating Lists](https://www.computerchess.org.uk/ccrl/4040/about.html) specifies time control of 40 moves in 15 minutes.  Below is
set for 15 **seconds**.  To make it minutes the change the tc vaue to 40/15:0

See https://www.computerchess.org.uk/ccrl/4040/about.html

```bash
cutechess-cli \
  -engine cmd=./bin/app.sh arg="-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 4 -bd 4" \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/15 \
  -pgnout log/output_pgn_file.pgn \
  -games 1 \
  -debug \
  > log/output_engine.log 2>&1
```

5. reformat the xml log files
```bash
xmlstarlet ed -L -O log/*.xml
```
6. copy the moves from `output_pgn_file.pgn` to the clipboard and paste them into Stockfish (Shift-Alt-N) so it is easy to replay the moves.  (
```bash
awk '/1. /,0' log/output_pgn_file.pgn | pbcopy
```
or simply
```bash
cat log/output_pgn_file.pgn | pbcopy
```

7. find the surprising move.
8. load the xml file that aligns to the ply with the surprise result and debug.  To quickly navigate to the right node use
```shell
ruby ./bin/xpath-to-leaf.rb log/FILE
```

or to show the best move for all moves:
```shell
./bin/pv.sh | sort
```


#### Helpful
This is useful to find the root nodes values.
```bash
xmlstarlet sel -t -m "/chess/iteration[last()]/node/node/summary" -c . -n ./log/game-20240226T173215-ply-2.xml```
```

I'm using Oxygen XML Editor to quickly navigate to the right level of the hierarchy.  I'd prefer an
open source tool that makes navigation quick and simple.

## References
* [Lc0 Testing guide](https://lczero.org/dev/wiki/testing-guide/)
* [Chess Tuning Tools](https://chess-tuning-tools.readthedocs.io/en/latest/index.html) - place parameteres to tune in a json file, and let cutechess-cli run tournaments to tune the values of those params.
* [SPRT testing the Rustic chess engine](https://rustic-chess.org/progress/sprt_testing.html) - using cutechess-cli
* Match Statistics on [chessprogrammingwiki.org](https://www.chessprogramming.org/Match_Statistics)
* [A few thoughts about testing chess engines](https://tearth.dev/posts/a-few-thoughts-about-testing-chess-engines/) blog - piece on using cutechess-cli