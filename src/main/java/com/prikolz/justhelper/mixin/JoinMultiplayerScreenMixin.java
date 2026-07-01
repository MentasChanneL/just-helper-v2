package com.prikolz.justhelper.mixin;

import com.prikolz.justhelper.UpdateChecker;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {
    @Inject(method = "join", at = @At("TAIL"))
    public void onJoin(ServerData serverData, CallbackInfo ci) {
        UpdateChecker.requireCheck = true;
    }
}
