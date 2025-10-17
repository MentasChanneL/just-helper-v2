package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.commands.arguments.FloorArgumentType;
import com.prikolz.justhelper.dev.BlockCodePos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class FloorCommand extends JustHelperCommand {
    public FloorCommand() {
        super("floor");
        this.description = "[Название или номер этажа] <gray>- Телепортация на этаж(Позиция по XZ не изменятся).";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument("floor", new FloorArgumentType()).executes( context -> {
                    int floor = context.getArgument("floor", Integer.class);
                    return execute(floor);
                })
        );
    }

    public static int execute(int floor) {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        CommandBuffer.add("tp " + player.getX() + " " + BlockCodePos.getY(floor) + " " + player.getZ());
        return 1;
    }
}
