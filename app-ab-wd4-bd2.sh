#!/bin/bash

# To package run
# $ mvn package assembly:single
bash -c "cd /Users/markmansour/IdeaProjects/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App -Dexec.args=\"-wd 4 -bd 2\""