package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.util.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public abstract class JustHelperCommand {

    public final String id;
    public String name;
    public String description = "Нет описания";

    public JustHelperCommand(String id) {
        this.id = id;
        name = id;
    }

    public final boolean isEnabled() {
        return Config.get().commandParameters.value.get(id).isEnabled();
    }

    public final LiteralArgumentBuilder<ClientSuggestionProvider> build() {
        name = Config.get().commandParameters.value.get(id).getName();
        return create( JustHelperCommands.literal( name ) );
    }

    public abstract LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main);

    public static int feedback(String m, Object ... placeholders) {
        return feedback(ComponentUtils.minimessage(m, placeholders));
    }

    public static int feedback(int value, String m, Object ... placeholders) {
        feedback(ComponentUtils.minimessage(m, placeholders));
        return value;
    }

    public static int feedback(Component message) {
        Minecraft.getInstance().player.displayClientMessage(message, false);
        return 0;
    }
}
