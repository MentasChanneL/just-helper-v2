package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.DevelopmentWorld;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GUIMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        DevelopmentWorld.render(guiGraphics, deltaTracker);
    }
}
