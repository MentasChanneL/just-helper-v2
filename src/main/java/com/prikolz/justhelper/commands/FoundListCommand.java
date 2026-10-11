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
                Commands.argument("page", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            AdvancedFindCommand.execute(AdvancedFindCommand.lastFound, IntegerArgumentType.getInteger(context, "page"));
                            return 1;
                        })
        );
    }
}
