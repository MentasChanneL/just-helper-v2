package com.prikolz.justhelper.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.dev.values.Variable;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JustHelperCommands {

    public static final HashMap<String, JustHelperCommand> commands = new HashMap<>();
    public static final List<JustHelperCommand> registerOrder = new ArrayList<>();

    public static void initialize() {
        register( new MainModCommand() );
        register( new FindCommand() );
        register( new FoundListCommand() );
        register( new FloorCommand() );
        register( new DescribeCommand() );
        register( new ItemEditorCommand() );
        register( new VarCommand(Variable.Scope.LOCAL, "vlc") );
        register( new VarCommand(Variable.Scope.GAME, "vg") );
        register( new VarCommand(Variable.Scope.SAVE, "vs") );
        register( new VarCommand(Variable.Scope.LINE, "vl") );
        register( new GetDataTypeCommand("n", " ", "num") );
        register( new GetDataTypeCommand("t", null, "txt") );
        register( new ZeroCommand() );
        register( new StupidCommand() );
        register( new Base64Command() );
        register( new ZlibCommand() );
    }

    public static void registerDispatcher(CommandDispatcher<ClientSuggestionProvider> dispatcher) {

        commands.values().forEach((v) -> {
            if (v.isEnabled()) dispatcher.register( v.build() );
        });

        JustHelperClient.LOGGER.info("Registered {} commands", commands.size());
    }

    private static void register(JustHelperCommand command) {
        registerOrder.add(command);
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
            if (command.startsWith(helperCommand.name + " ") || command.equals(helperCommand.name)) {
                try {
                    ParseResults<ClientSuggestionProvider> parse = dispatcher.parse(command, provider);
                    dispatcher.execute(parse);
                } catch (Throwable t) {
                    JustHelperCommand.feedback(
                       TextUtils.minimessage("<red>[Just Helper] <tr:command.exception:'" + t.getMessage() + "'>")
                    );
                    JustHelperClient.LOGGER.printStackTrace(t);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isJustHelperCommand(String string) {
        if (!string.startsWith("/")) return false;
        string = string.substring(1);
        for (JustHelperCommand helperCommand : commands.values()) {
            if (string.startsWith(helperCommand.name + " ") || string.equals(helperCommand.name)) return true;
        }
        return false;
    }

}
