package com.prikolz.justhelper.codespace;

import com.prikolz.justhelper.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Iterator;

public class CodeSpaceRender {

    private float updateCooldown = 0;

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
        var floor = CodeSpace.getFloorDescribe(pos.floor, true);
        floorText = floor == null ? Component.literal(pos.floor + " этаж")
                : floor.component.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)).append(" (" + floor.floor + ")");
        lineText = Component.literal(pos.line + " линия");
        var block = pos.toPos();
        var mat = level.getBlockState(block).getBlock();
        var sign = CodeSpace.signs.get(new BlockPos(block.getX(), block.getY(), block.getZ() + 1));
        blockText = Component.empty();
        if (sign != null) {
            var lines = sign.getLines();
            if (lines.length > 0) {
                blockText = Component.empty().append(Config.get().codeBlockNames.value.getName(mat))
                        .append(Component.literal("/" + lines[1]).setStyle(Style.EMPTY.withColor(0xFFFFFF)));
            }
        }
    }

    public void levelRender(
            ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections
    ) {
        var config = Config.get().codeSpaceRender.value.verticalRenderLimit.value;
        if (!CodeSpace.isActive() || !config.enabled.value) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        Iterator<SectionRenderDispatcher.RenderSection> iterator = visibleSections.iterator();
        while (iterator.hasNext()) {
            SectionRenderDispatcher.RenderSection section = iterator.next();
            BlockPos origin = section.getRenderOrigin();
            double dy = origin.getY() + 8 - player.getY();
            if (dy < 0) dy *= -1;
            if (dy > config.limit.value) iterator.remove();
        }
    }

    public void renderGUI(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (updateCooldown <= 0) {
            infoUpdate();
            updateCooldown = 5f;
        } else updateCooldown -= deltaTracker.getGameTimeDeltaTicks();
        if (Config.get().codeSpaceRender.value.showPosition.value) renderPosition(guiGraphics);
    }

    private void renderPosition(GuiGraphicsExtractor guiGraphics) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        int textWidth = font.width(floorText);
        int x = screenWidth - textWidth - 10;
        guiGraphics.text(font, floorText, x, 5, 0xFFFFFFFF);

        textWidth = font.width(lineText);
        x = screenWidth - textWidth - 10;
        guiGraphics.text(font, lineText, x, 20, 0xFFFFFFFF);

        textWidth = font.width(blockText);
        x = screenWidth - textWidth - 10;
        guiGraphics.text(font, blockText, x, 35, 0xFFFFFFFF);
    }
}
