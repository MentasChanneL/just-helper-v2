package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.dev.values.Number;
import com.prikolz.justhelper.util.JustHelperUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ZeroCommand extends JustHelperCommand {
    public ZeroCommand() {
        super("0");
        this.description = "<gray>- Коротка версия команды '/num 0'";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            JustHelperUtils.addItem(new Number("0").createItemStack());
            return 1;
        });
    }
}
