package com.prikolz.justhelper.commands;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.commands.arguments.SignsArgumentType;
import com.prikolz.justhelper.commands.arguments.SuggestionArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.List;

public class FindCommand extends JustHelperCommand {

    public FindCommand() {
        super("find");
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument(
                        "text", new SignsArgumentType()
                ).executes((context -> {
                    String text = StringArgumentType.getString(context, "text");
                    execute(text);
                    return 1;
                })
                )
        );
    }

    public void execute(String text) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

    }
}
