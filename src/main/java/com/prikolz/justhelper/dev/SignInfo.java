package com.prikolz.justhelper.dev;

import com.prikolz.justhelper.Config;
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

    public String getMiniBlockSprite() { return getMiniBlockSprite(true); }

    public String getMiniBlockSprite(boolean addHover) {
        var level = Minecraft.getInstance().level;
        if (level == null) return "";
        var blockState = Minecraft.getInstance().level.getBlockState(codePos.blockPos);
        var render = Minecraft.getInstance().getBlockRenderer();
        try {
            var sprite = render.getBlockModel(blockState).particleIcon().contents();
            var name = Config.get().codeBlockNames.value.getMiniName(blockState.getBlock());
            if (addHover)
                return "<hover:show_text:\"" + name + "\"><sprite:\"minecraft:blocks\":\"" + sprite.name().getPath() + "\"></hover>";
            else
                return "<sprite:\"minecraft:blocks\":\"" + sprite.name().getPath() + "\">";
        } catch (Throwable ignore) {}
        return "";
    }
}
