package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.world.item.Items;

import java.util.List;

public class UnknownValue extends DevValue {

    public UnknownValue() {
        super("null", Items.BARRIER, "Неизвестное значение");
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of();
    }
}
