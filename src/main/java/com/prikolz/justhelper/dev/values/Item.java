package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.JustMCUtils;
import com.prikolz.justhelper.util.Pair;

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
                    return new Item(JustMCUtils.gzipDecompress(item));
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
        var id = new StringBuilder();
        int[] subsequence = new int[]{ 105, 100, 0 };
        int value = 0;
        ReadMode mode = ReadMode.HEAD;
        for (char c : item.toCharArray()) {
            switch (mode) {
                case HEAD -> {
                    if ((int) c == subsequence[value]) {
                        value++;
                        if (value >= subsequence.length) mode = ReadMode.LENGTH;
                    } else {
                        value = 0;
                    }
                }
                case LENGTH -> {
                    value = c;
                    mode = ReadMode.ID;
                }
                case ID -> {
                    if (value <= 0) break;
                    id.append(c);
                    value--;
                }
            }
        }
        return List.of(Pair.of("id", id.toString()));
    }

    private enum ReadMode {
        HEAD, LENGTH, ID
    }
}
