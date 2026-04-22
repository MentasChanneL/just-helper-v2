package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.List;
import java.util.Map;

public class StupidCommand extends JustHelperCommand {
    private static final Map<Character, String> words = Map.of(
            '0', "рублей",
            '1', "рубль",
            '2', "рубля",
            '3', "рубля",
            '4', "рубля",
            '5', "рублей",
            '6', "рублей",
            '7', "рублей",
            '8', "рублей",
            '9', "рублей"
    );

    public StupidCommand() {
        super("ludi");
        this.description = "<gray>- null";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(JustHelperCommands.argument("amount", StringArgumentType.greedyString()).executes(context -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 0;
            String result = StringArgumentType.getString(context, "amount").replaceAll("[^0-9]", "");
            if (result.isEmpty()) {
                connection.sendChat("люди");
                return 1;
            }
            connection.sendChat(
                    "!люди, есть " + result + " " + words.getOrDefault(result.charAt(result.length() - 1), "рубля") + "?"
            );
            return 1;
        })).executes(context -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) return 0;
            connection.sendChat("!люди, есть 2 рубля?");
            return 1;
        });
    }
}
