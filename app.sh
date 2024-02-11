#!/bin/bash

echo "command line params are: $@"
# To package run
# $ mvn package assembly:single
bash -c "cd /Users/markmansour/IdeaProjects/marks-chess && mvn exec:java -Dexec.mainClass=com.stateofflux.chess.App -Dexec.args=\"$(echo $@)\""