package com.prikolz.justhelper.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class FileUtils {
    public static File getGameFolder() {
        return FabricLoader.getInstance().getGameDir().toFile();
    }

    public static File getConfigFolder() {
        return new File(getGameFolder().getPath() + "/config/justhelper");
    }

    public static File getWorldFolder(String uuid) {
        return new File(getConfigFolder() + "/worlds/" + uuid);
    }
}
