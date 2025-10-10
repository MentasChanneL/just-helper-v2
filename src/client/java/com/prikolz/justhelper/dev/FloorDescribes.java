package com.prikolz.justhelper.dev;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.mixin.client.DisplayMixin;
import com.prikolz.justhelper.mixin.client.TextDisplayMixin;
import com.prikolz.justhelper.util.ComponentUtils;
import com.prikolz.justhelper.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Vector3f;

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
    public final Map<Integer, Component> render = new HashMap<>();

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

    public void spawnDescribe(int floor, ClientLevel level) {
        if (level == null) return;
        var text = ComponentUtils.minimessage(describes.get(floor));
        var ent = entities.get(floor);
        if (ent != null) ent.remove(Entity.RemovalReason.KILLED);

        var textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);

        textDisplay.getEntityData().set(TextDisplayMixin.getDataTextID(), text, true);
        textDisplay.getEntityData().set(TextDisplayMixin.getDataBGColorID(), 0xFF5B5959, true);
        textDisplay.getEntityData().set(DisplayMixin.getDataScaleID(), new Vector3f(10), true);
        BlockPos pos = new BlockPos(-1, 4 + (7 * (floor - 1)), 47);
        textDisplay.setPos(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5);
        textDisplay.setYRot(90);

        level.addEntity(textDisplay);

        entities.put(floor, textDisplay);
        render.put(floor, text);
    }

    public void describe(int floor, String text) {
        if(!DevelopmentWorld.isActive()) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        describes.put(floor, floor + " " + text);
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
