package com.prikolz.justhelper.mixin.client;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayMixin {
    @Accessor("DATA_SCALE_ID")
    static EntityDataAccessor<Vector3f> getDataScaleID() { throw new AssertionError(); }
}
