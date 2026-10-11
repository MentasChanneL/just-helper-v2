package com.prikolz.justhelper.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public abstract class JustHelperCommand {

    public final String id;
    public String name;
    public String[] aliases;
    public final String[] defaultAliases;
    public String description = "Нет описания";

    public JustHelperCommand(String id, String ... aliases) {
        this.id = id;
        this.name = id;
        this.defaultAliases = aliases;
    }

    public final boolean isEnabled() {
        return Config.get().commandParameters.value.get(id).isEnabled();
    }

    public final void register(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        var config = Config.get().commandParameters.value.get(id);
        name = config.getName();
        dispatcher.register( create(Commands.literal(name)) );
        var commandAliases = config.aliases.value;
        aliases = commandAliases.toArray(new String[0]);
        for (String alias : aliases) dispatcher.register( create(Commands.literal(alias)) );
    }

    public abstract LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main);

    public static int feedback(String m, Object ... placeholders) {
        return feedback(TextUtils.minimessage(m, placeholders));
    }

    public static int feedback(int value, String m, Object ... placeholders) {
        feedback(TextUtils.minimessage(m, placeholders));
        return value;
    }

    public static int feedback(Component message) {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        player.sendSystemMessage(message);
        return 0;
    }
}
