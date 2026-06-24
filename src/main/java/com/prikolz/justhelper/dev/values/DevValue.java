package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.config.ValueFormats;
import com.prikolz.justhelper.util.NBTUtils;
import com.prikolz.justhelper.util.TextUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public abstract class DevValue {

    public static String DECORATION_TEXT_KEY = "justhelper_decoration_text";

    public static void setDecorationText(ItemStack item, String text, int color, int limit) {
        var customData = item.get(DataComponents.CUSTOM_DATA).copyTag();
        if (text.length() > limit) text = text.substring(0, limit);
        var compound = new CompoundTag();
        compound.put("text", StringTag.valueOf( text ));
        compound.put("color", IntTag.valueOf( color ));
        customData.put(DECORATION_TEXT_KEY, compound);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
    }

    public final String type;
    public Item material;
    public final String defaultStringFormat;
    public DevValueRegistry<DevValue> registry;
    public CompoundTag unusedFields = null;

    public DevValue(String type, Item material, String defaultStringFormat) {
        this.type = type;
        this.material = material;
        this.defaultStringFormat = defaultStringFormat;
        registry = (DevValueRegistry<DevValue>) DevValueRegistry.getRegistry(type);
    }

    public abstract List<Pair<String, String>> getFormatPlaceholders();
    public abstract String miniBuilder();
    public void handleItemStack(ItemStack item) {}
    public void itemDecoration(ItemStack item) {}

    public final String getStringFormat() {
        var placeholders = getFormatPlaceholders();
        var result = Config.get().valueFormats.value.getFormatted(type, placeholders);
        if (result == null) return ValueFormats.format(placeholders, defaultStringFormat);
        return result;
    }

    public final String getMiniVersion() {
        if (material instanceof BlockItem blockItem) {
            var render = Minecraft.getInstance().getBlockRenderer();
            try {
                var sprite = render.getBlockModel(blockItem.getBlock().defaultBlockState()).particleIcon().contents();
                return "<sprite:\"minecraft:blocks\":\"" + sprite.name().getPath() + "\"> " + miniBuilder();
            } catch (Throwable ignore) {}
        }
        var path = BuiltInRegistries.ITEM.getKey(material).getPath();
        return "<sprite:\"minecraft:items\":\"item/" + path + "\"> " + miniBuilder();
    }

    public CompoundTag toNBT() {
        var result = new CompoundTag();
        result.put("type", StringTag.valueOf(this.type));
        this.registry.nbtResolver.resolve(this, result);
        return result;
    }

    public final ItemStack createItemStack() {
        var item = new ItemStack(material);
        var value = new CompoundTag();
        value.put("type", StringTag.valueOf(type));
        registry.nbtResolver.resolve(this, value);
        var creativePlusTag = new CompoundTag();
        creativePlusTag.put("value", value);
        var customData = new CompoundTag();
        customData.put("creative_plus", creativePlusTag);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic>" + getStringFormat()));
        itemDecoration(item);
        return item;
    }

    public static <T extends DevValue> T fromNBT(CompoundTag nbt, boolean fullPath) {
        if (nbt == null) return null;
        var valueTag = fullPath ? NBTUtils.get(nbt, "creative_plus.value") : nbt;
        if (!(valueTag instanceof CompoundTag value)) return null;
        String type = value.getString("type").orElse(null);
        if (type == null) return null;
        var resolver = DevValueRegistry.getRegistry(type);
        if (resolver == null) return null;
        try {
            var result = (T) resolver.valueResolver.resolve(new DevValueRegistry.TagReader(value));
            result.registry = (DevValueRegistry<DevValue>) resolver;
            result.unusedFields = value;
            return result;
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("Can't resolve from NBT for '{}' data type: {} | NBT: {}", type, t.getMessage(), value);
            JustHelperClient.LOGGER.printStackTrace(t, JustHelperClient.JustHelperLogger.LogType.WARN);
            return null;
        }
    }

    public static <T extends DevValue> T fromItem(ItemStack item) {
        if (item == null || item.isEmpty()) return null;
        var data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var nbt = data.copyTag();
        return fromNBT(nbt, true);
    }

    public static void changeLore(ItemStack item, List<Component> add) {
        var lines = new ArrayList<Component>();
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore != null) lines.addAll(lore.lines());
        var index = -1;
        var i = 0;
        for (var line : lines) {
            if (line.getString().equals("-")) {
                index = i;
                break;
            }
            i++;
        }
        if (index != -1) while (lines.size() > index) lines.removeLast();
        lines.add(TextUtils.minimessage("<gray>-"));
        lines.addAll(add);
        lines.add(TextUtils.minimessage("<gray>-"));
        item.set(DataComponents.LORE, new ItemLore(lines));
    }

}
