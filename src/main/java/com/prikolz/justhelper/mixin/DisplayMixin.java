package com.prikolz.justhelper.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayMixin {
    @Accessor
    static EntityDataAccessor<Vector3f> getDATA_SCALE_ID() { throw new AssertionError(); }
}
