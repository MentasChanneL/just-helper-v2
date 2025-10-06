package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public abstract class JustHelperCommand {

    public final String id;
    public final String name;

    public JustHelperCommand(String id) {
        this.id = id;
        this.name = id;
    }

    public final LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        return create( JustHelperCommands.literal(name) );
    }

    public abstract LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main);

    public static int feedback(String message) {
        return feedback(Component.literal(message));
    }

    public static int feedback(Component message) {
        Minecraft.getInstance().player.displayClientMessage(message, false);
        return 0;
    }
}
