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

## References
* [Lc0 Testing guide](https://lczero.org/dev/wiki/testing-guide/)
* [Chess Tuning Tools](https://chess-tuning-tools.readthedocs.io/en/latest/index.html) - place parameteres to tune in a json file, and let cutechess-cli run tournaments to tune the values of those params.
* [SPRT testing the Rustic chess engine](https://rustic-chess.org/progress/sprt_testing.html) - using cutechess-cli
* Match Statistics on [chessprogrammingwiki.org](https://www.chessprogramming.org/Match_Statistics)
* [A few thoughts about testing chess engines](https://tearth.dev/posts/a-few-thoughts-about-testing-chess-engines/) blog - piece on using cutechess-cli