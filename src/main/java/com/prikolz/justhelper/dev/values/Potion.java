package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class Potion extends DevValue {

    public static final String type = "potion";
    public static final DevValueRegistry<Potion> registry = DevValueRegistry.create(
            Potion.type,
            nbt -> {
                var id = nbt.getString("potion").orElse("null");
                var duration = nbt.getInt("duration").orElse(0);
                var amplifier = nbt.getInt("amplifier").orElse(0);
                return new Potion(id, duration, amplifier);
            },
            (value, nbt) -> {
                nbt.put("potion", StringTag.valueOf(value.id));
                nbt.put("duration", IntTag.valueOf(value.duration));
                nbt.put("amplifier", IntTag.valueOf(value.amplifier));
            }
    );

    public String id;
    public int duration;
    public int amplifier;

    public Potion(String id, int duration, int amplifier) {
        super(Potion.type, Items.DRAGON_BREATH, "Эффект({id} {level}, {duration} т)");
        this.id = id;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of();
    }
}
