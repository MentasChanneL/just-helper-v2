package com.prikolz.justhelper.config;


import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public record CodeBlockNames(Map<String, String> names, Map<Block, String> registry) {

    public static Block getBlock(String key) {
        try {
            return BuiltInRegistries.BLOCK.getValue(Identifier.parse(key));
        } catch (Throwable t) {
            return null;
        }
    }

    public void add(Block block, String name) {
        var key = BuiltInRegistries.BLOCK.getKey(block).getPath();
        add(key, name);
    }

    public void add(String key, String name) {
        var block = getBlock(key);
        if (block == null) return;
        registry.put(block, key);
        names.put(key, name);
    }

    public Component getName(Block block) {
        var key = registry.get(block);
        if (key == null) return block.getName();
        var name = names.get(key);
        if (name == null) return block.getName();
        return TextUtils.minimessage(name);
    }
}
