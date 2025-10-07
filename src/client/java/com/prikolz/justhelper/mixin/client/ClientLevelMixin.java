package com.prikolz.justhelper.mixin.client;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "onBlockEntityAdded", at = @At("HEAD"))
    private void onBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
        DevelopmentWorld.addSign(blockEntity);
    }
}
