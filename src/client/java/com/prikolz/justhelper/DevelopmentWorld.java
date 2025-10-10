package com.prikolz.justhelper;

import com.prikolz.justhelper.dev.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.HashMap;
import java.util.Set;

public abstract class DevelopmentWorld {

    private static final String DEV_SUFFIX = "_creativeplus_editor";
    private static final String DEV_PREFIX = "world_";

    public static final HashMap<VariableType, VariablesHistory> history = new HashMap<>();
    public static final HashMap<BlockPos, SignInfo> signs = new HashMap<>();
    public static FloorDescribes describes = null;

    private static boolean enableRender = false;

    private static String worldUUID;

    public static boolean isActive() {
        var name = getWorldName();
        if (name == null) return false;
        return name.endsWith(DEV_SUFFIX) && name.startsWith(DEV_PREFIX);
    }

    private static String getWorldName() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        return level.dimension().location().getPath();
    }

    public static void initialize() {
        CommandBuffer.clear();
        if (!isActive()) {
            enableRender = false;
            worldUUID = null;
            history.forEach((k, v) -> v.save());
            history.clear();
            signs.clear();
            return;
        }
        var worldName = getWorldName();
        if (worldName == null) return;
        if (Config.get().showPositionInCode.value) enableRender = true;
        worldUUID = worldName.substring(DEV_PREFIX.length(), worldName.length() - DEV_SUFFIX.length());
        JustHelperClient.LOGGER.info("Joined to develop world {}", worldUUID);
        history.forEach((k, v) -> v.save());
        history.clear();
        signs.clear();
        history.put( VariableType.LOCAL, new VariablesHistory(worldUUID, VariableType.LOCAL) );
        history.put( VariableType.GAME, new VariablesHistory(worldUUID, VariableType.GAME) );
        history.put( VariableType.SAVE, new VariablesHistory(worldUUID, VariableType.SAVE) );
        describes = new FloorDescribes(worldUUID);
        describes.spawn();
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!enableRender) return;
        Minecraft minecraft = Minecraft.getInstance();
        var player  = minecraft.player;
        if (player == null) return;
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();

        BlockCodePos pos = new BlockCodePos(player.getBlockX(), player.getBlockY(), player.getBlockZ());

        Component describe = describes.render.get(pos.floor);
        Component text = describe == null ? Component.literal(pos.floor + " этаж") : describe;
        int textWidth = font.width(text);
        int x = screenWidth - textWidth - 10;
        guiGraphics.drawString(font, text, x, 5, 0xFFFFFFFF);

        text = Component.literal(pos.line + " линия");
        textWidth = font.width(text);
        x = screenWidth - textWidth - 10;
        guiGraphics.drawString(font, text, x, 20, 0xFFFFFFFF);
    }

    public static void addToHistory(VariableType type, String name) {
        if (history.isEmpty() || name == null) return;
        history.get(type).history.add(name);
    }

    public static Set<String> getVariablesHistory(VariableType type) {
        if (!history.containsKey(type)) return Set.of();
        return history.get(type).history;
    }

    public static void addSign(BlockEntity blockEntity) {
        if (!isActive()) return;
        if (!(blockEntity instanceof SignBlockEntity sign)) return;
        signs.put(sign.getBlockPos(), new SignInfo(sign));
    }
}
