package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.List;

public class StupidCommand extends JustHelperCommand {
    public StupidCommand() {
        super("ludi");
        this.description = "<gray>- null";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(JustHelperCommands.argument("amount", StringArgumentType.greedyString()).executes(context -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 0;
            String result = StringArgumentType.getString(context, "amount").replaceAll("[^0-9]", "");
            connection.sendChat("!люди, есть " + result + " рубля?");
            return 1;
        })).executes(context -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 0;
            connection.sendChat("!люди, есть 2 рубля?");
            return 1;
        });
    }
}
