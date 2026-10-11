package com.prikolz.justhelper.codespace;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.codespace.values.DevValue;
import com.prikolz.justhelper.codespace.values.Variable;
import com.prikolz.justhelper.commands.JustHelperCommand;
import com.prikolz.justhelper.commands.arguments.searching.FoundSignInfo;
import com.prikolz.justhelper.mixin.DisplayMixin;
import com.prikolz.justhelper.mixin.TextDisplayMixin;
import com.prikolz.justhelper.util.JustHelperUtils;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static com.prikolz.justhelper.JustHelperClient.GSON;

public abstract class CodeSpace {
    public static final String WORLD_PREFIX = "world_";
    public static final String WORLD_SUFFIX = "_creativeplus_editor";

    public static final HashMap<Variable.Scope, VariablesHistory> history = new HashMap<>();
    public static final HashMap<BlockPos, SignInfo> signs = new HashMap<>();
    public static final HashMap<Integer, FloorDescribe> floorDescribes = new HashMap<>();

    public static Vec3 anchor = null;
    private static String worldUUID = null;
    private static CodeSpaceRender render = null;
    private static Entity anchor3dMarker = null;
    private static PlayerTeam team = null;

    public static boolean isActive() {
        var name = getWorldName();
        if (name == null) return false;
        return name.endsWith(WORLD_SUFFIX) && name.startsWith(WORLD_PREFIX);
    }

    private static String getWorldName() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        return level.dimension().identifier().getPath();
    }

    public static void initialize(ClientLevel level) {
        CommandBuffer.clear();
        var worldName = getWorldName();
        if (worldName != null && worldName.startsWith(WORLD_PREFIX)) {
            var currentWorldUUID = worldName.substring(WORLD_PREFIX.length());
            if (currentWorldUUID.endsWith(WORLD_SUFFIX))
                currentWorldUUID = currentWorldUUID.substring(0, currentWorldUUID.length() - WORLD_SUFFIX.length());
            if (!currentWorldUUID.equals(worldUUID) && Config.get().autoWorldLimitBar.value) {
                CommandBuffer.add("world limit bar");
            }
            worldUUID = currentWorldUUID;
        } else {
            worldUUID = null;
        }
        if (!isActive()) {
            render = null;
            history.forEach((k, v) -> v.save());
            history.clear();
            signs.clear();
            floorDescribes.clear();
            return;
        }
        var scoreboard = level.getScoreboard();
        if (team != null) scoreboard.removePlayerTeam(team);
        team = scoreboard.addPlayerTeam("justhelper.aqua");
        team.setColor(Optional.of(TeamColor.AQUA));
        if (worldName == null) return;
        render = new CodeSpaceRender();
        JustHelperClient.LOGGER.info("Joined to develop world {}", worldUUID);
        history.forEach((k, v) -> v.save());
        history.clear();
        signs.clear();
        for (Variable.Scope scope : Variable.Scope.values()) {
            history.put( scope, new VariablesHistory(worldUUID, scope) );
        }
        loadDescribes();
        spawnAnchorMarker(level);
    }

    public static void tick() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (anchor3dMarker != null) {
            var scale = Math.max(Math.min((float) (player.distanceTo(anchor3dMarker) * 0.5), 20f), 1f);
            var data = anchor3dMarker.getEntityData();
            data.set(
                    DisplayMixin.DATA_SCALE_ID(),
                    new Vector3f(scale),
                    true
            );
            data.set(DisplayMixin.DATA_INTERPOLATION_DURATION_ID(), 1, true);
        }
    }

    public static CodeSpaceRender render() { return render; }

    public static void handleItemStack(ItemStack item) {
        if (!isActive()) return;
        var value = DevValue.fromItem(item);
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

    private static void spawnAnchorMarker(ClientLevel level) {
        if (anchor3dMarker != null) anchor3dMarker.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        if (anchor == null) return;
        var config = Config.get().teleportAnchor.value;
        if (!config.marker.value || !config.enabled.value) return;
        var marker = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, level);
        var data = marker.getEntityData();
        data.set(
                DisplayMixin.DATA_BRIGHTNESS_ID(),
                0xFF,
                true
        );
        data.set(
                DisplayMixin.DATA_BILLBOARD_ID(),
                (byte) 3,
                true
        );
        data.set(
                TextDisplayMixin.DATA_STYLE_FLAGS_ID(),
                (byte) (((byte) 0) | 2),
                true
        );
        data.set(
                TextDisplayMixin.DATA_TEXT_ID(),
                TextUtils.minimessage(config.icon.value),
                true
        );
        data.set(
                TextDisplayMixin.DATA_BACKGROUND_COLOR_ID(),
                0,
                true
        );
        marker.setPos(new Vec3(0, 0.5, 0).add(anchor));
        anchor3dMarker = marker;
        level.addEntity(marker);
        marker.setGlowingTag(true);
        level.getScoreboard().addPlayerToTeam(marker.getUUID() + "", team);
    }

    public static void teleportAnchor() {
        if (!isActive()) return;
        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        if (player == null || level == null) return;
        var pos = new BlockCodePos(4, player.getBlockY(), player.getBlockZ());
        anchor = player.position();
        var config = Config.get().teleportAnchor.value;
        spawnAnchorMarker(level);
        if (!config.enabled.value || !config.sendMessage.value) return;
        var signInfo = SignInfo.getSign(pos);
        String hover;
        String display;
        if (signInfo != null) {
            var found = FoundSignInfo.create(signInfo);
            hover = found.createHoverInfo(":3");
            if (found.lines().length > 1)
                display = "<white>" + signInfo.getMiniBlockSprite() + "<aqua> " + found.lines()[1];
            else
                display = found.lines()[0];
        } else {
            hover = "<gray>Нажмите для телепортации";
            var describe = floorDescribes.get(pos.floor);
            String floor = describe == null ? pos.floor + " э" : describe.minimessage;
            display = "<yellow>" + floor + "<reset><white>/<yellow>" + pos.line + " л<white>/<yellow>" + pos.pos + " п";
        }
        JustHelperCommand.feedback(
                "\n<aqua>{5}<reset><click:run_command:'/tp {1} {2} {3}'><hover:show_text:'{0}'> <white>Вернутся на <aqua>>> {4} <aqua><<\n",
                hover,
                player.getX(),
                player.getY(),
                player.getZ(),
                display,
                config.icon.value
        );
    }

    public static int getFloor(String text) {
        if (!CodeSpace.isActive()) return -1;
        try {
            return Integer.parseInt(text);
        } catch (Exception ignore) {
            String lower = text.toLowerCase();
            for (var describe : CodeSpace.getFloorDescribes()) {
                if (describe.plain.toLowerCase().contains(lower)) return describe.floor;
            }
            return -1;
        }
    }

    public static Collection<FloorDescribe> getFloorDescribes() { return floorDescribes.values(); }

    public static FloorDescribe getFloorDescribe(int floor, boolean nullable) {
        var result = floorDescribes.get(floor);
        if (result == null && !nullable) return FloorDescribe.empty(floor);
        return result;
    }

    public static void setFloorDescribe(int floor, String text) {
        if (!CodeSpace.isActive()) return;
        var level = Minecraft.getInstance().level;
        var describe = floorDescribes.get(floor);
        if (describe == null) {
            describe = new FloorDescribe(floor, text);
            floorDescribes.put(floor, describe);
            if (level != null) describe.spawn(level);
            updateDescribesFile();
            return;
        }
        describe.setText(text);
        if (level != null) describe.spawn(level);
        updateDescribesFile();
    }

    public static boolean deleteFloorDescribe(int floor) {
        if (!CodeSpace.isActive()) return false;
        var describe = floorDescribes.remove(floor);
        if (describe == null) return false;
        describe.removeEntity();
        updateDescribesFile();
        return true;
    }

    private static void loadDescribes() {
        File configFile = JustHelperUtils.getDescribesFile(worldUUID);
        if (!configFile.exists() || configFile.isDirectory()) return;
        try {
            var level = Minecraft.getInstance().level;
            JsonObject json = GSON.fromJson(GSON.newJsonReader(new FileReader(configFile)), JsonObject.class);
            for (String key : json.keySet()) {
                var floor = Integer.parseInt(key);
                var value = json.get(key);
                var describe = new FloorDescribe(floor, value.getAsString());
                floorDescribes.put(floor, describe);
                if (level != null) describe.spawn(level);
            }
            JustHelperClient.LOGGER.info("{} floor describes", floorDescribes.size());
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Failed to read 'describes.json' for world '{}': {}", worldUUID, t.getMessage());
        }
    }

    private static void updateDescribesFile() {
        File configFile = JustHelperUtils.getDescribesFile(worldUUID);
        var json = new JsonObject();
        floorDescribes.forEach((k, v) -> {
            json.add(k.toString(), new JsonPrimitive(v.minimessage));
        });
        String jsonStr = GSON.toJson(json);
        try {
            Files.createDirectories(JustHelperUtils.getWorldFolder(worldUUID).toPath());
            Files.writeString(configFile.toPath(), jsonStr);
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Failed to save describe: {}", t.getMessage());
        }
    }
}
