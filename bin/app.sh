#!/bin/bash

# To package run
# $ mvn package assembly:single
bash -c "cd /Users/markmansour/IdeaProjects/marks-chess && java -jar target/markschess-1.0-SNAPSHOT-jar-with-dependencies.jar $(echo $@)"