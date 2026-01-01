package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.config.ValueFormats;
import com.prikolz.justhelper.util.TextUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public abstract class DevValue {

    public final String type;
    public final Item material;
    public final String defaultStringFormat;
    public DevValueRegistry<DevValue> registry = null;
    public CompoundTag unusedFields = null;

    public DevValue(String type, Item material, String defaultStringFormat) {
        this.type = type;
        this.material = material;
        this.defaultStringFormat = defaultStringFormat;
    }

    public abstract List<Pair<String, String>> getFormatPlaceholders();
    public void handleItemStack(ItemStack item) {}

    public final String getStringFormat() {
        var placeholders = getFormatPlaceholders();
        var result = Config.get().valueFormats.value.getFormatted(type, placeholders);
        if (result == null) return ValueFormats.format(placeholders, defaultStringFormat);
        return result;
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
