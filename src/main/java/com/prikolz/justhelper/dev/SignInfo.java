package com.prikolz.justhelper.dev;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class SignInfo {
    public final BlockPos pos;
    public final BlockCodePos codePos;

    public SignInfo(SignBlockEntity sign) {
        pos = sign.getBlockPos();
        codePos = new BlockCodePos(pos.getX(), pos.getY(), pos.getZ() - 1);
    }

    public static SignInfo getSign(BlockCodePos pos) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var loc = pos.toPos();
        var state = level.getBlockEntity(new BlockPos(loc.getX(), loc.getY(), loc.getZ() + 1));
        if (state == null) return null;
        if (!(state instanceof SignBlockEntity sign)) return null;
        return new SignInfo(sign);
    }

    public String[] getLines() {

        var level = Minecraft.getInstance().level;
        if (level == null) return new String[0];
        var ent = Minecraft.getInstance().level.getBlockEntity(pos);
        if (ent == null) return new String[0];
        if (!(ent instanceof SignBlockEntity sign)) return new String[0];

        String[] lines = new String[4];

        var i = 0;
        for (Component line : sign.getFrontText().getMessages(false)) {
            lines[i] = line.getString();
            i++;
        }
        return lines;
    }
}
