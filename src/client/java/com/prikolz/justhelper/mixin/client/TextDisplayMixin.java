package com.prikolz.justhelper.mixin.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayMixin {
    @Accessor("DATA_TEXT_ID")
    static EntityDataAccessor<Component> getDataTextID() { throw new AssertionError(); }

    @Accessor("DATA_BACKGROUND_COLOR_ID")
    static EntityDataAccessor<Integer> getDataBGColorID() { throw new AssertionError(); }
}
