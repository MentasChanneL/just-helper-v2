package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.dev.values.DevValueRegistry;
import com.prikolz.justhelper.gui.TextEditorScreen;
import com.prikolz.justhelper.util.JustMCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class TextEditorCommand extends JustHelperCommand {
    public TextEditorCommand() {
        super("te");
        this.description = "<gray>- Редактор текста";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.executes(context -> {
            var client = Minecraft.getInstance();
            if (
                    client.getConnection() == null || client.player == null ||
                    !DevelopmentWorld.isActive() || !JustMCUtils.isTextValue(client.player.getMainHandItem())
            ) return 0;

            client.schedule(() ->
                    client.setScreen(TextEditorScreen.create(DevValueRegistry.fromItem(client.player.getMainHandItem())))
            );

            return 1;
        });
    }
}
