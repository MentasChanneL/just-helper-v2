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

    public static void register(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        commands.clear();

        register( dispatcher, new FindCommand() );

        JustHelperClient.LOGGER.info("Registered {} commands", commands.size());
    }

    private static void register(
            CommandDispatcher<ClientSuggestionProvider> dispatcher,
            JustHelperCommand command
    ) {
        dispatcher.register( command.build() );
        commands.put(command.id, command);
    }

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
                       ComponentUtils.minimessage("<red><tr:command.exception:'" + t.getMessage() + "'>")
                    );
                }
                return true;
            }
        }
        return false;
    }

}
