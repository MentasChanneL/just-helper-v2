package com.prikolz.justhelper.dev;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.DevelopmentWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DevRender {

    private int updateCooldown = 0;

    private BlockCodePos pos = new BlockCodePos(0, 0, 0);
    private Component floorText = Component.empty();
    private Component lineText = Component.empty();
    private Component blockText = Component.empty();

    private final Font font = Minecraft.getInstance().font;
    private final Minecraft minecraft = Minecraft.getInstance();

    private void infoUpdate() {
        var level = minecraft.level;
        var player = minecraft.player;
        if (player == null || level == null) return;
        pos = new BlockCodePos(4, player.getBlockY(), player.getBlockZ());
        Component describe = DevelopmentWorld.describes.render.get(pos.floor);
        floorText = (describe == null ? Component.literal(pos.floor + " этаж") : describe).copy().setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
        lineText = Component.literal(pos.line + " линия");
        var block = pos.toPos();
        var mat = level.getBlockState(block).getBlock();
        var sign = DevelopmentWorld.signs.get(new BlockPos(block.getX(), block.getY(), block.getZ() + 1));
        blockText = Component.empty();
        if (sign != null) {
            var lines = sign.getLines();
            if (lines.length > 0) {
                blockText = Component.empty().append(Config.get().codeBlockNames.value.getName(mat))
                        .append(Component.literal("/" + lines[1]).setStyle(Style.EMPTY.withColor(0xFFFFFF)));
            }
        }

    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (updateCooldown <= 0) {
            infoUpdate();
            updateCooldown = 20;
        }
        updateCooldown--;
        if (Config.get().showPositionInCode.value) renderPosition(guiGraphics);
    }

    private void renderPosition(GuiGraphics guiGraphics) {

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        int textWidth = font.width(floorText);
        int x = screenWidth - textWidth - 10;
        guiGraphics.drawString(font, floorText, x, 5, 0xFFFFFFFF);

        textWidth = font.width(lineText);
        x = screenWidth - textWidth - 10;
        guiGraphics.drawString(font, lineText, x, 20, 0xFFFFFFFF);

        textWidth = font.width(blockText);
        x = screenWidth - textWidth - 10;
        guiGraphics.drawString(font, blockText, x, 35, 0xFFFFFFFF);
    }
}
