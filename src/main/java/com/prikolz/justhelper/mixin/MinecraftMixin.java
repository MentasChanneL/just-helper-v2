package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Mutable
    private User user;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        this.user = JustHelperClient.user;

    }

    @Inject(method = "setLevel", at = @At("TAIL"))
    public void onSetLevel(ClientLevel level, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        try {
            DevelopmentWorld.initialize();
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Develop world initialization error: {}", t.getMessage());
        }
    }
}
