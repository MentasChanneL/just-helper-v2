package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.ComponentUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class Array extends DevValue {

    public static final String type = "array";
    public static DevValueRegistry<Array> registry = DevValueRegistry.create(
            Array.type,
            nbt -> {
                var valuesTag = nbt.getList("values").orElse(null);
                if (valuesTag == null) throw new NullPointerException("Values is null");
                var list = new ArrayList<DevValue>();
                for (Tag tag : valuesTag) {
                    if (!(tag instanceof CompoundTag valueTag)) continue;
                    var value = DevValueRegistry.fromNBT(valueTag, false);
                    if (value == null) value = new UnknownValue();
                    list.add(value);
                }

                return new Array(list);
            },
            (value, nbt) -> {
                var valuesTag = new ListTag();
                for (var entry : value.values) {
                    if (entry.registry == null) continue;
                    valuesTag.add( DevValueRegistry.toNBT(entry) );
                }
                nbt.put("values", valuesTag);
            }
    );
    public List<DevValue> values;

    public Array(List<DevValue> values) {
        super(type, Items.GUSTER_BANNER_PATTERN, "Список({values})");
        this.values = values;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var lines = new ArrayList<Component>();
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore != null) lines.addAll(lore.lines());
        lines.add(Component.literal(" "));
        int line = 0;
        for (var entry : values) {
            var key = entry.getStringFormat();
            if (key.length() > 30) key = key.substring(0, 30) + "...";
            lines.add( ComponentUtils.minimessage("<aqua><italic:false>{0}", key) );
            line++;
            if (line > 21) {
                lines.add( ComponentUtils.minimessage("<gray>...") );
                break;
            }
        }
        item.set(DataComponents.LORE, new ItemLore(lines));
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        StringBuilder values = new StringBuilder();
        int i = 0;
        for (var value : this.values) {
            values.append(value.getStringFormat());
            i++;
            if (i < this.values.size()) values.append(", ");
        }
        return List.of( Pair.of("values", values.toString()) );
    }
}
