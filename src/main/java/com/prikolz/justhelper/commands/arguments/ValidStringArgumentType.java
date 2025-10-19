package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.util.ComponentUtils;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ValidStringArgumentType implements ArgumentType<String> {

    public static final DynamicCommandExceptionType STRING_NO_MATCHES = new DynamicCommandExceptionType((object) -> ComponentUtils.minimessage("Строка не соответствует формату {0}", object));

    private final Pattern pattern;
    private final String feedback;

    public ValidStringArgumentType() {
        this("^[a-z_0-9]+$", "a-z_0-9");
    }

    public ValidStringArgumentType(String pattern, String feedback) {
        this.pattern = Pattern.compile(pattern);
        this.feedback = feedback;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String result = StringArgumentType.string().parse(reader);

        if (!pattern.matcher(result).matches()) throw STRING_NO_MATCHES.create(feedback);

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }
}
