package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.world.item.Items;

import java.util.List;

public class Vector extends DevValue {

    public static final String type = "vector";
    public static final DevValueRegistry<Vector> registry = DevValueRegistry.create(
            Vector.type,
            nbt -> {
                var x = nbt.getDouble("x").orElse(0.0);
                var y = nbt.getDouble("y").orElse(0.0);
                var z = nbt.getDouble("z").orElse(0.0);
                return new Vector(x, y, z);
            },
            (value, nbt) -> {
                nbt.put("x", DoubleTag.valueOf(value.x));
                nbt.put("y", DoubleTag.valueOf(value.y));
                nbt.put("z", DoubleTag.valueOf(value.z));
            }
    );

    public double x;
    public double y;
    public double z;

    public Vector(double x, double y, double z) {
        super(type, Items.PRISMARINE_SHARD, "Вектор({x}, {y}, {z})");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private String round(double value) {
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("x", round(x)),
                Pair.of("y", round(y)),
                Pair.of("z", round(z))
        );
    }
}
