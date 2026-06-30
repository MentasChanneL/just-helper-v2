package com.prikolz.justhelper.util;

import com.prikolz.justhelper.dev.values.DevValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.*;

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

    public static String zlibCompress(String str) {
        var input = str.getBytes();
        var output = new byte[input.length * 4];
        var compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();
        int l = compressor.deflate(output);
        byte[] arr = new byte[l];
        System.arraycopy(output, 0, arr, 0, l);
        return Base64.getEncoder().encodeToString(arr);
    }

    public static String zlibDecompress(String base64) throws DataFormatException {
        byte[] arr = Base64.getDecoder().decode(base64);
        var inflater = new Inflater();
        var outStream = new ByteArrayOutputStream();
        var buffer = new byte[2048];
        inflater.setInput(arr);
        int c = -1;
        while (c != 0) {
            c = inflater.inflate(buffer);
            outStream.write(buffer, 0, c);
        }
        inflater.end();
        return outStream.toString(StandardCharsets.UTF_8);
    }

    public static String gzipCompress(String str) throws IOException {
        if (str == null || str.isEmpty()) return "";
        byte[] input = str.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(input);
            gzip.finish();
            byte[] compressed = baos.toByteArray();
            return Base64.getEncoder().encodeToString(compressed);
        }
    }

    public static String gzipDecompress(String base64) {
        byte[] arr = Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(arr);
            GZIPInputStream gzip = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
