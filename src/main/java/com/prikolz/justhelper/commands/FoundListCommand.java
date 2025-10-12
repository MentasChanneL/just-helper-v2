package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class FoundListCommand extends JustHelperCommand {
    public FoundListCommand() {
        super("foundlist");
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument("page", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            FindCommand.execute(FindCommand.lastFound, IntegerArgumentType.getInteger(context, "page"));
                            return 1;
                        })
        );
    }
}
