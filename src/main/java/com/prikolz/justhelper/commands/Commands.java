package com.prikolz.justhelper.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.codespace.values.Variable;
import com.prikolz.justhelper.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Commands {

    public static final HashMap<String, JustHelperCommand> commands = new HashMap<>();
    public static final List<JustHelperCommand> registerOrder = new ArrayList<>();

    public static void initialize() {
        register( new MainModCommand() );
        register( new AdvancedFindCommand() );
        register( new BackCommand() );
        register( new BuildPlayCommand() );
        register( new FoundListCommand() );
        register( new FloorCommand() );
        register( new DescribeCommand() );
        register( new ItemEditorCommand() );
        register( new PosCommand("point", true) );
        register( new PosCommand("topoint", false) );
        register( new VarCommand(Variable.Scope.LOCAL, "vlc") );
        register( new VarCommand(Variable.Scope.GAME, "vg") );
        register( new VarCommand(Variable.Scope.SAVE, "vs") );
        register( new VarCommand(Variable.Scope.LINE, "vl") );
        register( new ValueCommand("n", " ", ValueCommand.Type.NUMBER, true) );
        register( new ValueCommand("t", null, ValueCommand.Type.TEXT, false) );
        register( new ValueCommand("tl", null, ValueCommand.Type.TEXT_LEGACY, false) );
        register( new ValueCommand("ts", null, ValueCommand.Type.TEXT_MINI, false) );
        register( new ValueCommand("tj", null, ValueCommand.Type.TEXT_JSON, false) );
        register( new ZeroCommand() );
        register( new StupidCommand() );
        register( new Base64Command() );
        register( new ZlibCommand() );
        register( new GzipCommand() );
        register( new AliasCommand("envg", "env var list game $name", Pair.of("name", StringArgumentType.greedyString())) );
        register( new AliasCommand("envs", "env var list save $name", Pair.of("name", StringArgumentType.greedyString())) );
    }

    public static void registerDispatcher(CommandDispatcher<ClientSuggestionProvider> dispatcher) {

        commands.values().forEach((v) -> {
            if (v.isEnabled()) v.register(dispatcher);
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
        if (isJustHelperCommand(command) || isJustHelperCommand("/" + command)) {
            try {
                ParseResults<ClientSuggestionProvider> parse = dispatcher.parse(command, provider);
                dispatcher.execute(parse);
            } catch (Throwable t) {
                var message = Component.literal("[JustHelper] ")
                        .append(Component.translatable("command.exception", t.getMessage()))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                JustHelperCommand.feedback(message);
                JustHelperClient.LOGGER.printStackTrace(t);
            }
            return true;
        }
        return false;
    }

    public static boolean isJustHelperCommand(String string) {
        if (!string.startsWith("/")) return false;
        string = string.substring(1);
        for (JustHelperCommand helperCommand : commands.values()) {
            if (string.startsWith(helperCommand.name + " ") || string.equals(helperCommand.name)) return true;
            for (var alias : helperCommand.aliases)
                if (string.startsWith(alias + " ") || string.equals(alias)) return true;
        }
        return false;
    }

}
