package com.prikolz.justhelper.mixin.client;

import com.mojang.brigadier.CommandDispatcher;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.commands.JustHelperCommand;
import com.prikolz.justhelper.commands.JustHelperCommands;
import com.prikolz.justhelper.dev.VariablesHistory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
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
    private ClientSuggestionProvider suggestionsProvider;

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSendCommand(String command, CallbackInfo ci) {
        if (JustHelperCommands.handleCommand(command, suggestionsProvider, commands)) ci.cancel();
    }

    @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
    private void sendUnattendedCommand(String string, @Nullable Screen screen, CallbackInfo ci) {
        if (JustHelperCommands.handleCommand(string, suggestionsProvider, commands)) ci.cancel();
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    public void onHandleCommands(ClientboundCommandsPacket clientboundCommandsPacket, CallbackInfo ci) {
        JustHelperCommands.register(commands);
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    public void onHandleContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!DevelopmentWorld.isActive()) return;
        for (ItemStack item : packet.items()) VariablesHistory.handleItemStack(item);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    public void onHandleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        VariablesHistory.handleItemStack(packet.getItem());
    }
}
