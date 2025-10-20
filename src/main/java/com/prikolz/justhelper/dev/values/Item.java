package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.JustMCUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Item extends DevValue {

    public static final String type = "item";
    public static final DevValueRegistry<Item> registry = DevValueRegistry.create(
            Item.type,
            nbt -> {
                var item = nbt.getString("item").orElse(null);
                if (item == null) return new Item("null");
                try {
                    return new Item(JustMCUtils.zlibDecompress(item.getBytes(StandardCharsets.UTF_8)));
                } catch (Throwable t) { throw new RuntimeException(t.getMessage()); }
            },
            (value, nbt) -> {
                throw new RuntimeException("FIX IT");
            }
    );

    public String item;

    public Item(String item) {
        super(Item.type, null, "Предмет({id})");
        this.item = item;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of();
    }
}
