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
  -debug  `# shows output from each engine`
```
