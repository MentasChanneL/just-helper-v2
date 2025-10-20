package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.ComponentUtils;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class GameValue extends DevValue {

    public static final String type = "game_value";
    public static final DevValueRegistry<GameValue> registry = DevValueRegistry.create(
            GameValue.type,
            nbt -> {
                var gameValue = nbt.getString("game_value").orElse(null);
                var selection = nbt.getString("selection").orElse("null");
                if (gameValue == null) throw new NullPointerException("Game value is null");
                return new GameValue(selection, gameValue);
            },
            (value, nbt) -> {
                nbt.put("game_value", StringTag.valueOf(value.gameValue));
                nbt.put("selection", StringTag.valueOf(value.selection));
            }
    );

    public String selection;
    public String gameValue;

    public GameValue(String selection, String gameValue) {
        super(GameValue.type, Items.NAME_TAG, "Игровое значение({id}, {selection})");
        this.selection = selection;
        this.gameValue = gameValue;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var lines = new ArrayList<Component>();
        lines.add(ComponentUtils.minimessage("<gray><italic:false>ID: {0}", this.gameValue));
        DevValue.changeLore(item, lines);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("id", this.gameValue),
                Pair.of("selection", this.selection)
        );
    }
}
