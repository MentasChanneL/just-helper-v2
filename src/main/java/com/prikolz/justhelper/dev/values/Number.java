package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class Number extends DevValue {

    public static final String type = "number";
    public static final DevValueRegistry<Number> registry = DevValueRegistry.register(
            Number.type,
            nbt -> {
                var valueTag = nbt.get("number");
                if (valueTag == null) throw new NullPointerException("Number is null");
                return new Number(valueTag.toString());
            },
            (value, nbt) -> {
                nbt.put("number", StringTag.valueOf(value.value));
            }
    );

    public String value;

    public Number(String value) {
        super(Number.type, Items.SLIME_BALL, "Число({value})");
        this.value = value;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of( Pair.of("value", this.value) );
    }
}
