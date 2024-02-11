package com.stateofflux.chess;

import com.beust.jcommander.Parameter;

public class AppArgs {
    @Parameter(names={"--white-player", "-w"}, order = 2)
    public String whiteStrategy = "AlphaBetaPlayer";

    @Parameter(names={"--black-player", "-b"}, order = 3)
    public String blackStrategy = "AlphaBetaPlayer";

    @Parameter(names={"--white-depth", "-wd"}, order = 4)
    public int whiteDepth = 2;

    @Parameter(names={"--black-depth", "-bd"}, order = 5)
    public int blackDepth = 2;

    @Parameter(names={"--evaluator", "-e"}, order = 6)
    public String evaluatorStrategy = "SimpleEvaluator";

    @Parameter(names={"--help", "-h"}, order = 1)
    public boolean askedForHelp = false;
}
