#!/bin/bash

# To package run
# $ mvn package assembly:single
/bin/bash -c 'cd /Users/markmansour/IdeaProjects/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App'
