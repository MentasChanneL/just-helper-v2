package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ZeroCommand extends JustHelperCommand {
    public ZeroCommand() {
        super("0");
        this.description = "<gray>- Коротка версия команды '/num 0'";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            CommandBuffer.add("num 0");
            return 1;
        });
    }
}
