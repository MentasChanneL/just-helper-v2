package com.prikolz.justhelper.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class TestCommand extends JustHelperCommand {

    public TestCommand() {
        super("bebebebe");
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main
                .then(JustHelperCommands.literal("ihihiha"))
                .then(JustHelperCommands.literal("prikol"))
                .executes((context -> {
                    return feedback("hello mate");
                }));
    }
}
