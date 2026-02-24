package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setLevel", at = @At("TAIL"))
    public void onSetLevel(ClientLevel clientLevel, CallbackInfo ci) {
        try {
            DevelopmentWorld.initialize();
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Develop world initialization error: {}", t.getMessage());
        }
    }
}
