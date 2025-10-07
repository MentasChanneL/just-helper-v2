package com.prikolz.justhelper.dev;

public class BlockCodePos {

    public final int floor;
    public final int line;
    public final int pos;

    public BlockCodePos(int x, int y, int z) {
        floor = (y - 5) / 7 + 1;
        line = z / 4;
        pos = (x - 4) / 2 + 1;
    }

}
