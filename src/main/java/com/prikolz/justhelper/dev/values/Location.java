package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.world.item.Items;

import java.util.List;

public class Location extends DevValue {

    public static final String type = "location";
    public static final DevValueRegistry<Location> registry = DevValueRegistry.register(
            Location.type,
            nbt -> {
                var x = nbt.getDouble("x").orElse(0.0);
                var y = nbt.getDouble("y").orElse(0.0);
                var z = nbt.getDouble("z").orElse(0.0);
                var yaw = nbt.getDouble("yaw").orElse(0.0);
                var pitch = nbt.getDouble("pitch").orElse(0.0);

                return new Location(x, y, z, yaw, pitch);
            },
            (value, nbt) -> {
                nbt.put("x", DoubleTag.valueOf(value.x));
                nbt.put("y", DoubleTag.valueOf(value.y));
                nbt.put("z", DoubleTag.valueOf(value.z));
                nbt.put("yaw", DoubleTag.valueOf(value.yaw));
                nbt.put("pitch", DoubleTag.valueOf(value.pitch));
            }
    );

    public Double x;
    public Double y;
    public Double z;
    public Double yaw;
    public Double pitch;

    public Location(Double x, Double y, Double z, Double yaw, Double pitch) {
        super(Location.type, Items.MAP, "Местоположение({x} {y} {z} | {yaw} {pitch})");
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("x", this.x + ""),
                Pair.of("y", this.y + ""),
                Pair.of("z", this.z + ""),
                Pair.of("yaw", this.yaw + ""),
                Pair.of("pitch", this.pitch + "")
        );
    }
}
