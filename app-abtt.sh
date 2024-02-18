#!/bin/bash

# To package run
# $ mvn package assembly:single
# -Xms<size>        set initial Java heap size
# -Xmx<size>        set maximum Java heap size
# -Xss<size>        set java thread stack size

bash -c "cd /Users/markmansour/IdeaProjects/marks-chess && MAVEN_OPTS=\"-Xms1024m -Xmx2048m\" mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App -Dexec.args=\"-w AlphaBetaPlayerWithTT -b AlphaBetaPlayerWithTT -wd 100 -bd 100\""