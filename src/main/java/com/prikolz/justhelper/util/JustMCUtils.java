package com.prikolz.justhelper.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.*;

public class JustMCUtils {
    public static boolean isTextValue(ItemStack item) {
        // простые метрики для отсеивания очевидных несоответствий
        if(item == null || item.isEmpty() || item.getItem() != Items.BOOK) return false;

        var nbt = item.get(DataComponents.CUSTOM_DATA);
        if(nbt == null) return false;
        var compound = nbt.copyTag();
        CompoundTag tag = (CompoundTag) NBTUtils.get(compound, "creative_plus.value");

        return tag.getString("type").orElse(null) != null &&
                tag.getString("text").orElse(null) != null &&
                tag.getString("parsing").orElse(null) != null;
    }

    public static ItemStack setTextValue(ItemStack item, String text) {
        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = (customData != null) ? customData.copyTag() : new CompoundTag();
        CompoundTag creativePlusCompound = root.getCompound("creative_plus").orElse(null);
        CompoundTag valueCompound = creativePlusCompound.getCompound("value").orElse(null);
        valueCompound.putString("text", text);
        creativePlusCompound.put("value", valueCompound);
        root.put("creative_plus", creativePlusCompound);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<white>" + TextUtils.cut(text, 150)));

        return item;
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
        var buffer = new byte[1024];
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
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
