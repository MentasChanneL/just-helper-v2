package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.config.ValueFormats;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public abstract class DevValue {

    public final String type;
    public final Item material;
    public final String defaultStringFormat;

    public DevValue(String type, Item material, String defaultStringFormat) {
        this.type = type;
        this.material = material;
        this.defaultStringFormat = defaultStringFormat;
    }

    public abstract List<Pair<String, String>> getFormatPlaceholders();

    public final String getStringFormat() {
        var placeholders = getFormatPlaceholders();
        var result = Config.get().valueFormats.value.getFormatted(type, placeholders);
        if (result == null) return ValueFormats.format(placeholders, defaultStringFormat);
        return result;
    }

}
