package com.prikolz.justhelper.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NBTUtils {
    public static Tag get(CompoundTag container, String path) {
        try {
            var args = path.split("\\.");
            CompoundTag result = container;
            for (String step : args) result = (CompoundTag) result.get(step);
            return result;
        } catch (Throwable t) {
            return null;
        }
    }
}
