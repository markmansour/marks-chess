#!/bin/bash

# Define the file path
script_dir=$(dirname "$0")
chess_jar="$script_dir/../target/markschess-1.0-SNAPSHOT-jar-with-dependencies.jar"
chess_jar=$(realpath "$chess_jar")

# Check if the file does not exist
if [ ! -f "$chess_jar" ]; then
    # Print an error message to stderr
    echo "Error: File '$chess_jar' not found." >&2
    # Exit the script with a non-zero exit status
    exit 1
fi

# To package run
# $ mvn package assembly:single
bash -c "java -jar $chess_jar $(echo $@)"