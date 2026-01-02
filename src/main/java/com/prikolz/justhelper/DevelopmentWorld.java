package com.prikolz.justhelper;

import com.prikolz.justhelper.commands.FindCommand;
import com.prikolz.justhelper.commands.JustHelperCommand;
import com.prikolz.justhelper.commands.arguments.SignsSearchingArgumentType;
import com.prikolz.justhelper.dev.*;
import com.prikolz.justhelper.dev.values.DevValueRegistry;
import com.prikolz.justhelper.dev.values.Variable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.HashMap;
import java.util.Set;

public abstract class DevelopmentWorld {

    private static final String DEV_SUFFIX = "_creativeplus_editor";
    private static final String DEV_PREFIX = "world_";

    public static final HashMap<Variable.Scope, VariablesHistory> history = new HashMap<>();
    public static final HashMap<BlockPos, SignInfo> signs = new HashMap<>();
    public static FloorDescribes describes = null;

    private static DevRender render = null;

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
            render = null;
            worldUUID = null;
            history.forEach((k, v) -> v.save());
            history.clear();
            signs.clear();
            return;
        }
        var worldName = getWorldName();
        if (worldName == null) return;
        render = new DevRender();
        worldUUID = worldName.substring(DEV_PREFIX.length(), worldName.length() - DEV_SUFFIX.length());
        JustHelperClient.LOGGER.info("Joined to develop world {}", worldUUID);
        history.forEach((k, v) -> v.save());
        history.clear();
        signs.clear();
        for (Variable.Scope scope : Variable.Scope.values()) {
            history.put( scope, new VariablesHistory(worldUUID, scope) );
        }
        describes = new FloorDescribes(worldUUID);
        describes.spawn();
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (render != null) render.render(guiGraphics, deltaTracker);
    }

    public static void handleItemStack(ItemStack item) {
        if (!isActive()) return;
        var value = DevValueRegistry.fromItem(item);
        if (value == null) return;

        value.handleItemStack(item);
    }

    public static void addToHistory(Variable.Scope type, String name) {
        if (history.isEmpty() || name == null) return;
        history.get(type).history.add(name);
    }

    public static Set<String> getVariablesHistory(Variable.Scope type) {
        if (!history.containsKey(type)) return Set.of();
        return history.get(type).history;
    }

    public static void addSign(BlockEntity blockEntity) {
        if (!isActive()) return;
        if (!(blockEntity instanceof SignBlockEntity sign)) return;
        signs.put(sign.getBlockPos(), new SignInfo(sign));
    }

    public static void teleportAnchor() {
        if (!isActive()) return;
        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        if (player == null || level == null) return;
        var pos = new BlockCodePos(player.getBlockX(), player.getBlockY(), player.getBlockY());
        if (!Config.get().teleportAnchor.value) return;
        var signInfo = SignInfo.getSign(pos);
        String hover;
        String display;
        if (signInfo != null) {
            var found = SignsSearchingArgumentType.FoundSignInfo.create(signInfo);
            hover = found.createHoverInfo(":3");
            var line = found.lines()[0];
            if (found.lines().length > 1) line = line + "/" + found.lines()[1];
            display = line;
        } else {
            hover = "<gray>Нажмите для телепортации";
            var floor = describes.describes.get(pos.floor);
            if (floor == null) floor = pos.floor + " э";
            display = "<yellow>" + floor + "<white>/<yellow>" + pos.line + " л<white>/<yellow>" + pos.pos + " п";
        }
        JustHelperCommand.feedback(
                "\n<click:run_command:'/tp {1} {2} {3}'><hover:show_text:'{0}'><aqua>⚓ <white>Вернутся на <aqua>>> {4} <aqua><<\n",
                hover,
                player.getX(),
                player.getY(),
                player.getZ(),
                display
        );
    }
}
