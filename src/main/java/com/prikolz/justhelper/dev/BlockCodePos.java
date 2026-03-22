package com.prikolz.justhelper.dev;

import com.prikolz.justhelper.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class BlockCodePos {

    public final int floor;
    public final int line;
    public final int pos;

    public final BlockPos blockPos;

    public BlockCodePos(int x, int y, int z) {
        floor = (y - 5) / 7 + 1;
        line = z / 4;
        pos = (x - 4) / 2 + 1;
        blockPos = new BlockPos(x, y, z);
    }

    public String getMiniBlockName() {
        var level = Minecraft.getInstance().level;
        if (level == null) return "";
        return Config.get().codeBlockNames.value.getMiniName(level.getBlockState(blockPos).getBlock());
    }

    public static int getX(int pos) { return 2 * pos + 2; }
    public static int getY(int floor) { return 7 * floor - 2; }
    public static int getZ(int line) { return line * 4; }

    public BlockPos toPos() {
        return new BlockPos(getX(pos), getY(floor) , getZ(line));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BlockCodePos codeBlockPos)) return false;
        return codeBlockPos.floor == this.floor && codeBlockPos.line == this.line && codeBlockPos.pos == this.pos;
    }

}
