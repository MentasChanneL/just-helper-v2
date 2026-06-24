package com.prikolz.justhelper.util;

import com.prikolz.justhelper.dev.values.DevValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

import java.io.File;

public class JustHelperUtils {
    public static File getGameFolder() {
        return FabricLoader.getInstance().getGameDir().toFile();
    }

    public static File getConfigFolder() {
        return new File(getGameFolder().getPath() + "/config/justhelper");
    }

    public static File getWorldFolder(String uuid) {
        return new File(getConfigFolder() + "/worlds/" + uuid);
    }

    public static void setItem(int slot, ItemStack item) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.getInventory().setItem(slot, item);
        var remoteSlot = slot;
        if (slot < 9 && slot >= 0) remoteSlot += 36;
        if (slot > 35 && slot < 40) remoteSlot -= 31;
        if (slot == 40) remoteSlot = 45;
        player.connection.send(
                new ServerboundSetCreativeModeSlotPacket(remoteSlot, item)
        );
    }

    public static boolean addItem(ItemStack item) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        var slot = player.getInventory().getFreeSlot();
        if (slot < 0) return false;
        setItem(slot, item);
        return true;
    }

}
