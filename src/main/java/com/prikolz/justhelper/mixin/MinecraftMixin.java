package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.UpdateChecker;
import com.prikolz.justhelper.util.JustHelperUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        CommandBuffer.tick(50);
        JustHelperUtils.resolveRunQueue();
    }

    @Inject(method = "setLevel", at = @At("TAIL"))
    public void onSetLevel(ClientLevel clientLevel, CallbackInfo ci) {
        try {
            DevelopmentWorld.initialize();
            UpdateChecker.onJoinCheckMessage();
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Develop world initialization error: {}", t.getMessage());
        }
    }
}
