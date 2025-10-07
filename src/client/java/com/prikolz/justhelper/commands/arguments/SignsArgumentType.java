package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SignsArgumentType implements ArgumentType<String> {

    public static final DynamicCommandExceptionType REQUIRED_REFERENCE = new DynamicCommandExceptionType((object) -> Component.translatableEscape("command.unknown.argument", object));

    private static final StringArgumentType parser = StringArgumentType.greedyString();

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return parser.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        DevelopmentWorld.signs.values().forEach((v) -> {
            for (String line : v.getLines()) builder.suggest(line);
        });
        return builder.buildFuture();
    }

}
