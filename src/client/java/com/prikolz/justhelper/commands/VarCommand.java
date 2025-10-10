package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.commands.arguments.VariableHistoryArgumentType;
import com.prikolz.justhelper.dev.VariableType;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class VarCommand extends JustHelperCommand {

    private final VariableType type;

    public VarCommand(VariableType type) {
        super("v" + type.id.charAt(0));
        this.type = type;
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument(
                        "names",
                        new VariableHistoryArgumentType(type)
                ).executes((context -> {
                    execute(StringArgumentType.getString(context, "names") );
                    return 1;
                }))
        ).executes(context -> {
            execute("");
            return 1;
        });
    }

    public void execute(String names) {
        if (!DevelopmentWorld.isActive()) return;
        for (String name : names.split("`")) {
            if (name.startsWith(" ")) name = name.substring(1);
            CommandBuffer.add("var " + type.id + " " + name);
        }
    }
}
