package com.prikolz.justhelper.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.commands.FindCommand;
import com.prikolz.justhelper.commands.JustHelperCommands;
import com.prikolz.justhelper.dev.BlockCodePos;
import com.prikolz.justhelper.dev.VariablesHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow
    private CommandDispatcher<ClientSuggestionProvider> commands;

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private ClientSuggestionProvider suggestionsProvider;

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String command, CallbackInfo ci) {
        if (JustHelperCommands.handleCommand(command, suggestionsProvider, commands)) ci.cancel();
    }

    @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
    private void sendUnattendedCommand(String command, @Nullable Screen screen, CallbackInfo ci) {
        if (JustHelperCommands.handleCommand(command, suggestionsProvider, commands)) {
            ci.cancel();
            return;
        }
        if (command.startsWith("tp") || command.startsWith("teleport") || command.startsWith("editor teleport")) {
            var args = command.split(" ");
            DevelopmentWorld.teleportAnchor();
            try {
                var pos = new BlockCodePos(
                        (int) Double.parseDouble(args[1]),
                        (int) Double.parseDouble(args[2]),
                        (int) Double.parseDouble(args[3])
                );
                FindCommand.findEach(pos);
            } catch (Throwable t) {
                JustHelperClient.LOGGER.warn("Fail find each: {}", t.getMessage());
            }
        }
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    public void onHandleCommands(ClientboundCommandsPacket clientboundCommandsPacket, CallbackInfo ci) {
        JustHelperCommands.registerDispatcher(commands);
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    public void onHandleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!DevelopmentWorld.isActive()) return;
        for (ItemStack item : packet.items()) DevelopmentWorld.handleItemStack(item);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    public void onHandleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        DevelopmentWorld.handleItemStack(packet.getItem());
    }
}
