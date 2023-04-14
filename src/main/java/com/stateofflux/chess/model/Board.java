package com.stateofflux.chess.model;

import java.util.HexFormat;

public class Board {
    protected byte[] pieces;

    public Board() {
        this.pieces = HexFormat.ofDelimiter(":").parseHex("e0:4f:d0:20:ea:3a:69:10:a2:d8:08:00:2b:30:30:9d");
    }
}
