package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class GetDataTypeCommand extends JustHelperCommand {

    private final String split;

    public GetDataTypeCommand(String id, String split) {
        super(id);
        this.split = split;
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument("arg", StringArgumentType.greedyString()).executes(context -> {
                    var arg = context.getArgument("arg", String.class);
                    var args = split == null ? new String[]{arg} : arg.split(split);
                    for (String a : args) CommandBuffer.add(a);
                    return 1;
                })
        );
    }
}
