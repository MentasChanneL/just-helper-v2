package com.prikolz.justhelper.dev;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.FileUtils;
import com.prikolz.justhelper.util.NBTUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class VariablesHistory {

    public Set<String> history;
    public final VariableType type;
    public final String worldUUID;
    public final File file;

    public VariablesHistory(String worldUUID, VariableType type) {
        this.type = type;
        this.worldUUID = worldUUID;
        this.file = getFile(worldUUID, type.id + "_history.txt");
        this.history = readFile(file);
        JustHelperClient.LOGGER.info("{} history size: {}", type.id, history.size());
    }

    private File getFile(String world, String name) {
        return new File(FileUtils.getConfigFolder().getPath() + "/worlds/" + world + "/" + name);
    }

    private Set<String> readFile(File file) {
        JustHelperClient.LOGGER.info("Reading {} history for world {}", type.id, worldUUID);
        var result = new HashSet<String>();
        if (!file.isFile()) return result;
        try (var reader = new BufferedReader(new FileReader(file))) {
            result.add(reader.readLine());
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("File '{}' read error: {}", file.getPath(), t.getMessage());
        }
        return result;
    }

    public void save() {
        JustHelperClient.LOGGER.info("Saving {} history for world {}", type.id, worldUUID);
        try { org.apache.commons.io.FileUtils.createParentDirectories(file); } catch (Throwable t) {
            JustHelperClient.LOGGER.error("File '{}' saving error: {}", file.getPath(), t.getMessage());
            return;
        }
        try (var writer = new FileWriter(file)) {
            history.forEach((el) -> {
                try {
                    writer.write(el);
                    writer.write("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("File '{}' saving error: {}", file.getPath(), t.getMessage());
        }
    }

    public static void handleItemStack(ItemStack item) {
        if (!DevelopmentWorld.isActive()) return;
        var customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        var valueTag = NBTUtils.get(customData.copyTag(), "creative_plus.value");
        if (!(valueTag instanceof CompoundTag value)) return;
        var type = value.getString("type").orElse(null);
        var scope = VariableType.getByID( value.getString("scope").orElse(null) );
        var name = value.getString("variable").orElse(null);
        if (scope == null || name == null || type == null || !type.equals("variable")) return;
        DevelopmentWorld.addToHistory(scope, name);
    }
}
