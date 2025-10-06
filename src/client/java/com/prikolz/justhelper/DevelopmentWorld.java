package com.prikolz.justhelper;

import com.prikolz.justhelper.dev.VariableType;
import com.prikolz.justhelper.dev.VariablesHistory;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

public abstract class DevelopmentWorld {

    private static final String DEV_SUFFIX = "_creativeplus_editor";
    private static final String DEV_PREFIX = "world_";

    public static final HashMap<VariableType, VariablesHistory> history = new HashMap<>();

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
        if (!isActive()) {
            worldUUID = null;
            history.forEach((k, v) -> v.save());
            history.clear();
            return;
        }
        var worldName = getWorldName();
        if (worldName == null) return;
        worldUUID = worldName.substring(DEV_PREFIX.length(), worldName.length() - DEV_SUFFIX.length());
        JustHelperClient.LOGGER.info("Joined to develop world {}", worldUUID);
        history.forEach((k, v) -> v.save());
        history.clear();
        history.put( VariableType.LOCAL, new VariablesHistory(worldUUID, VariableType.LOCAL) );
        history.put( VariableType.GAME, new VariablesHistory(worldUUID, VariableType.GAME) );
        history.put( VariableType.SAVE, new VariablesHistory(worldUUID, VariableType.SAVE) );

    }

    public static void addToHistory(VariableType type, String name) {
        if (history.isEmpty()) return;
        history.get(type).history.add(name);
    }
}
