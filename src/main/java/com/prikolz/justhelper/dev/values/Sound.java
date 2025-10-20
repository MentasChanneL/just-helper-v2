package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Items;

import java.util.List;

public class Sound extends DevValue {

    public static final String type = "sound";
    public static final DevValueRegistry<Sound> registry = DevValueRegistry.create(
            Sound.type,
            nbt -> {
                var sound = nbt.getString("sound").orElse("null");
                var source = nbt.getString("source").orElse("null");
                var variation = nbt.getString("variation").orElse("");
                var pitch = nbt.getDouble("pitch").orElse(1.0);
                var volume = nbt.getDouble("volume").orElse(1.0);
                return new Sound(sound, source, variation, volume, pitch);
            },
            (value, nbt) -> {
                nbt.put("sound", StringTag.valueOf(value.sound));
                nbt.put("source", StringTag.valueOf(value.source));
                nbt.put("variation", StringTag.valueOf(value.variation));
                nbt.put("pitch", DoubleTag.valueOf(value.pitch));
                nbt.put("volume", DoubleTag.valueOf(value.volume));
            }
    );

    public String sound;
    public String source;
    public String variation;
    public double volume;
    public double pitch;

    public Sound(String sound, String source, String variation, double volume, double pitch) {
        super(type, Items.NAUTILUS_SHELL, "Звук({id}, {volume}, {pitch})");
        this.sound = sound;
        this.source = source;
        this.variation = variation;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("id", sound),
                Pair.of("volume", String.valueOf(volume)),
                Pair.of("pitch", String.valueOf(pitch))
        );
    }
}
