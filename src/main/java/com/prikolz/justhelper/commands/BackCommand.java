package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class BackCommand extends JustHelperCommand {

    public static Vec3 prevPos = null;

    public BackCommand() {
        super("back");
        this.description = "<gray>- Вернет вас на место, откуда вы были телепортированны.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            if (prevPos == null) return JustHelperCommand.feedback("<yellow>JustHelper >> Перемещений не было, вернуться некуда");
            CommandBuffer.add("tp " + prevPos.x + " " + prevPos.y + " " + prevPos.z);
            return 1;
        });
    }
}
