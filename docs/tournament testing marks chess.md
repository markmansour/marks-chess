# Tournament Testing

## Using cutechess
### Prepare cutechess
To run on MacOS, follow [compilation guide on the cutechess wiki](https://github.com/cutechess/cutechess/wiki/Building-from-source#macos).  For ease of execution, also run `make install`.

### Preparing Mark's Chess
I've written Mark's Chess to accept command line parameters for the depth and player type.  Unfortunately I cannot 
invoke Mark's Chess with command line params from the cutechess-cli.  To get around this problem I could either 
a) write a wrapper shell script to invoke Mark's Chess with the appropriate arguments; or 
b) make these setting UCI setting
e.g. 
`
setoption name whiteplayer value AlphaBetaWithTT
setoption name white-depth value 200
`

I've chosen to write a simple wrapper script per config type.

### Running a tournament

```bash
cd $MARKS_CHESS_DIR
cutechess-cli \
  -engine cmd=./app-abtt.sh  `# marks chess engine configuration wrapped in a shell script` \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" `# have stockfish run at about 1350 ELO` \
  -each proto=uci tc=40/20 \
  -pgnout output_pgn_file.pgn \
  -concurrency 5 \
  -games 10 \
  -debug  `# shows output from each engine` \
  > output_engine.log 2>&1 &
```

### Reviewing Games
#### Evaluator and Alpha-Beta debugging
0. Remove the old log files:
```bash
rm log/* output_engine.log output_pgn_file.pgn

```

1. change the level from info to debug in `logback.xml`.  While you're there, make sure the logfile is going to the right directory.

```xml
<logger name="com.stateofflux.chess.alpha-beta-debugging" level="debug">
   <appender-ref ref="alpha-beta-file-debugger" />
</logger>
```

2. Rebuild the package `mvn package assembly:single`
3. Run cutechess-cli for a single game:
```bash
cutechess-cli \
  -engine cmd=./app-abtt.sh \
  -engine cmd=/usr/local/bin/stockfish option.UCI_LimitStrength=true "option.Use NNUE=false" \
  -each proto=uci tc=40/20 \
  -pgnout output_pgn_file.pgn \
  -games 1 \
  -debug \
  > output_engine.log 2>&1 &
```

4. copy the moves from `output_pgn_file.pgn` to the clipboard and paste them into Stockfish (Shift-Alt-N) so it is easy to replay the moves.  (one liner `awk '/1. /,0' output_pgn_file.pgn | pbcopy`)
5. find the surprising move.
6. reformat the xml log files `xmlstarlet ed -L -O log/*.xml`
7. load the xml file that aligns to the ply with the surprise result and debug.

## References
* [Lc0 Testing guide](https://lczero.org/dev/wiki/testing-guide/)
* [Chess Tuning Tools](https://chess-tuning-tools.readthedocs.io/en/latest/index.html) - place parameteres to tune in a json file, and let cutechess-cli run tournaments to tune the values of those params.
* [SPRT testing the Rustic chess engine](https://rustic-chess.org/progress/sprt_testing.html) - using cutechess-cli
* Match Statistics on [chessprogrammingwiki.org](https://www.chessprogramming.org/Match_Statistics)
* [A few thoughts about testing chess engines](https://tearth.dev/posts/a-few-thoughts-about-testing-chess-engines/) blog - piece on using cutechess-cli