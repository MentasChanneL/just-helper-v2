package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.TextUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class Dictionary extends DevValue {

    public static final String type = "map";
    public static final DevValueRegistry<Dictionary> registry = DevValueRegistry.create(
            Dictionary.type,
            nbt -> {
                var values = nbt.getCompound("values").orElse(null);
                if (values == null) throw new NullPointerException("Values is null");
                var listValues = new ArrayList<Pair<DevValue, DevValue>>();
                for (String key : values.keySet()) {
                    CompoundTag value = values.getCompound(key).orElse(null);
                    if (value == null) continue;
                    if (value.isEmpty()) continue;
                    var index = key.indexOf("{");
                    if (index == -1) continue;
                    key = key.substring(index);
                    CompoundTag tag;
                    try {
                        tag = TagParser.parseCompoundFully(key);
                    } catch (Throwable t) {
                        JustHelperClient.LOGGER.warn("(Dictionary) Fail parse NBT: {}", key);
                        continue;
                    }
                    DevValue keyValue = DevValueRegistry.fromNBT(tag, false);
                    if (keyValue == null) keyValue = new UnknownValue();
                    DevValue valueValue = DevValueRegistry.fromNBT(value, false);
                    if (valueValue == null) keyValue = new UnknownValue();
                    listValues.add( Pair.of(keyValue, valueValue) );
                }
                return new Dictionary(listValues);
            },
            (value, nbt) -> {
                throw new RuntimeException("FIX IT");
                //CompoundTag values = new CompoundTag();
                //int i = 0;
                //for (var entry : value.values) {
                //    var tag = new CompoundTag();
                //    tag.put("type", StringTag.valueOf(entry.first.type));
                //}
            }
    );

    public final List<Pair<DevValue, DevValue>> values;

    public Dictionary(List<Pair<DevValue, DevValue>> values) {
        super(Dictionary.type, Items.CHEST_MINECART, "Словарь({values})");
        this.values = values;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var lines = new ArrayList<Component>();
        int line = 0;
        for (var entry : values) {
            if (entry == null || entry.first == null || entry.second == null) continue;
            var key = entry.first.getStringFormat();
            var value = entry.second.getStringFormat();
            if (key.length() > 30) key = key.substring(0, 30) + "...";
            if (value.length() > 30) value = value.substring(0, 30) + "...";
            lines.add( TextUtils.minimessage("<yellow><italic:false>{0} <gray>= <white>{1}", key, value) );
            line++;
            if (line > 21) {
                lines.add( TextUtils.minimessage("<gray>...") );
                break;
            }
        }
        DevValue.changeLore(item, lines);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        var values = new StringBuilder();
        for (var entry : this.values) {
            var formatKey = entry.first == null ? "null" : entry.first.getStringFormat();
            var formatValue = entry.second == null ? "null" : entry.second.getStringFormat();
            values.append(formatKey).append(" = ").append(formatValue);
        }
        return List.of( Pair.of("values", values.toString()) );
    }
}
