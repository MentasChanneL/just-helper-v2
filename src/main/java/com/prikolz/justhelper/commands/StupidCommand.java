package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class StupidCommand extends JustHelperCommand {
    public StupidCommand() {
        super("ludi");
        this.description = "<gray>- null";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 0;
            connection.sendChat("!люди, есть 2 рубля?");
            return 1;
        });
    }
}
