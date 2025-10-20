package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Items;

import java.util.List;

public class Particle extends DevValue {

    public static final String type = "particle";
    public static final DevValueRegistry<Particle> registry = DevValueRegistry.create(
            Particle.type,
            nbt -> {
                var particle = nbt.getString("particle_type").orElse("null");
                var count = nbt.getInt("count").orElse(0);
                var size = nbt.getInt("size").orElse(0);
                var first_spread = nbt.getDouble("first_spread").orElse(0.0);
                var second_spread = nbt.getDouble("second_spread").orElse(0.0);
                return new Particle(particle, count, size, first_spread, second_spread);
            },
            (value, nbt) -> {
                nbt.put("particle_type", StringTag.valueOf(value.particle));
                nbt.put("count", IntTag.valueOf(value.count));
                nbt.put("size", DoubleTag.valueOf(value.size));
                nbt.put("first_spread", DoubleTag.valueOf(value.firstSpread));
                nbt.put("second_spread", DoubleTag.valueOf(value.secondSpread));
            }
    );

    public String particle;
    public int count;
    public double size;
    public double firstSpread;
    public double secondSpread;

    public Particle(String particle, int count, double size, double first_spread, double second_spread) {
        super(type, Items.PHANTOM_MEMBRANE, "Частица({id}, {count}, {size}, {spread1}, {spread2})");
        this.particle = particle;
        this.size = size;
        this.count = count;
        this.firstSpread = first_spread;
        this.secondSpread = second_spread;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of();
    }
}
