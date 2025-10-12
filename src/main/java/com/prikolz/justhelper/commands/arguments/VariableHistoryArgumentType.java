package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.dev.VariableType;

import java.util.concurrent.CompletableFuture;

public class VariableHistoryArgumentType implements ArgumentType<String> {

    private static final StringArgumentType parser = StringArgumentType.greedyString();
    private final VariableType type;
    private String lastInput = "";

    public VariableHistoryArgumentType(VariableType type) {
        this.type = type;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        lastInput = parser.parse(reader);
        return lastInput;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var history = DevelopmentWorld.getVariablesHistory(type);
        if (lastInput.length() < 2) {
            for (String key : history) builder.suggest(key);
            return builder.buildFuture();
        }
        for (String name : lastInput.split("`")) {
            if (name.startsWith(" ")) name = name.substring(1);
            for (String key : history) if (key.contains(name)) builder.suggest(key);
        }
        return builder.buildFuture();
    }
}
