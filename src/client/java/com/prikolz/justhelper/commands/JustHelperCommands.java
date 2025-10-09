package com.prikolz.justhelper.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.ComponentUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.HashMap;

public class JustHelperCommands {

    public static HashMap<String, JustHelperCommand> commands = new HashMap<>();

    public static void initialize() {
        register( new FindCommand() );
        register( new FoundListCommand() );
        register( new DescribeCommand() );
        register( new VarLocalCommand() );
    }

    public static void registerDispatcher(CommandDispatcher<ClientSuggestionProvider> dispatcher) {

        commands.values().forEach((v) -> {
            if (v.isEnabled()) dispatcher.register( v.build() );
        });

        JustHelperClient.LOGGER.info("Registered {} commands", commands.size());
    }

    private static void register(JustHelperCommand command) { commands.put(command.id, command); }

    public static LiteralArgumentBuilder<ClientSuggestionProvider> literal(String string) {
        return LiteralArgumentBuilder.literal(string);
    }

    public static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(String string, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(string, argumentType);
    }

    public static boolean handleCommand(
            String command,
            ClientSuggestionProvider provider,
            CommandDispatcher<ClientSuggestionProvider> dispatcher
    ) {
        for (JustHelperCommand helperCommand : commands.values()) {
            if (command.startsWith(helperCommand.name)) {
                try {
                    ParseResults<ClientSuggestionProvider> parse = dispatcher.parse(command, provider);
                    dispatcher.execute(parse);
                } catch (Throwable t) {
                    JustHelperCommand.feedback(
                       ComponentUtils.minimessage("<red>[Just Helper] <tr:command.exception:'" + t.getMessage() + "'>")
                    );
                }
                return true;
            }
        }
        return false;
    }

}
