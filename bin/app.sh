#!/bin/bash

# get the version number
# commented out as it takes too long
# chess_version=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)

# get the version number
chess_version="1.1-SNAPSHOT"

# Define the file path
script_dir=$(dirname "$0")
chess_jar="$script_dir/../target/markschess-$chess_version-jar-with-dependencies.jar"
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