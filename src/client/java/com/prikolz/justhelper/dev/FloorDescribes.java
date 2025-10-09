package com.prikolz.justhelper.dev;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.mixin.client.TextDisplayMixin;
import com.prikolz.justhelper.util.ComponentUtils;
import com.prikolz.justhelper.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.prikolz.justhelper.JustHelperClient.GSON;

public class FloorDescribes {

    public final String world;
    public final Map<Integer, String> describes = new HashMap<>();
    public final Map<Integer, Entity> entities = new HashMap<>();

    private static File getConfigFile(String worldUUID) {
        return new File(FileUtils.getWorldFolder(worldUUID).getPath() + "/describes.json");
    }

    public FloorDescribes(String worldUUID) {
        this.world = worldUUID;
        File configFile = getConfigFile(worldUUID);
        if (!configFile.exists() || configFile.isDirectory()) return;
        try {
            JsonObject json = GSON.fromJson(GSON.newJsonReader(new FileReader(configFile)), JsonObject.class);
            for (String key : json.keySet()) {
                var floor = Integer.parseInt(key);
                var value = json.get(key);
                describes.put(floor, value.getAsString());
            }
            JustHelperClient.LOGGER.info("{} floor describes", describes.size());
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Failed to read 'describes.json' for world '{}': {}", world, t.getMessage());
        }
    }

    public void spawn() {
        var level = Minecraft.getInstance().level;
        for (int floor : describes.keySet()) spawnDescribe(floor, level);
    }

    public void spawnDescribe(int floor, Level level) {
        if (level == null) return;
        var text = ComponentUtils.minimessage(describes.get(floor));
        if (text == null) return;
        var ent = entities.get(floor);
        if (ent != null) ent.remove(Entity.RemovalReason.KILLED);
        var textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        textDisplay.getEntityData().set(TextDisplayMixin.getDataTextID(), text, true);
        BlockPos pos = new BlockPos(-1, 4 + (7 * (floor - 1)), 47);
        textDisplay.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        level.addFreshEntity(textDisplay);
        entities.put(floor, textDisplay);
    }

    public void describe(int floor, String text) {
        if(!DevelopmentWorld.isActive()) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        describes.put(floor, floor + ". " + text);
        File configFile = getConfigFile(world);
        var json = new JsonObject();
        describes.forEach((k, v) -> {
            json.add(k.toString(), new JsonPrimitive(v));
        });
        String jsonStr = GSON.toJson(json);
        try {
            Files.createDirectories(FileUtils.getWorldFolder(world).toPath());
            Files.writeString(configFile.toPath(), jsonStr);
            spawnDescribe(floor, level);
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Failed to save describe: {}", t.getMessage());
        }
    }
}
