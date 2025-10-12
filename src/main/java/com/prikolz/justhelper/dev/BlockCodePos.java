package com.prikolz.justhelper.dev;

import com.prikolz.justhelper.component.BlockStateComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

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

    public BlockPos toPos() {
        return new BlockPos(2 * pos + 2, 7 * floor - 2, line * 4);
    }

    public Component getBlockComponent() {
        var level = Minecraft.getInstance().level;
        if (level == null) return Component.empty();
        var state = level.getBlockState(blockPos);
        return BlockStateComponent.create(state);
    }

}
