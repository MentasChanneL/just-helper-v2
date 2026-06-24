package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class Number extends DevValue {

    public static final String type = "number";
    public static final DevValueRegistry<Number> registry = DevValueRegistry.create(
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
    public void handleItemStack(ItemStack item) {
        var str = value.replaceAll("[^-0-9.]", "");
        setDecorationText(item, str, NamedTextColor.WHITE.value());
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><red>" + value.replaceAll("[^-0-9.]", "")));
        handleItemStack(item);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of( Pair.of("value", this.value) );
    }

    @Override
    public String miniBuilder() {
        return value;
    }
}
