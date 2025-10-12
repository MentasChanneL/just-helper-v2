package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SuggestionArgumentType implements ArgumentType<String> {

    public static final DynamicCommandExceptionType REQUIRED_REFERENCE = new DynamicCommandExceptionType((object) -> Component.translatableEscape("command.unknown.argument", object));

    private final Collection<String> list;
    private final StringArgumentType parser;
    private final boolean isRequired;

    public SuggestionArgumentType(Collection<String> reference, boolean isGreedy, boolean isRequired) {
        this.list = reference;
        this.isRequired = isRequired;
        parser = isGreedy ? StringArgumentType.greedyString() : StringArgumentType.string();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String result = parser.parse(reader);
        if (isRequired && !list.contains(result)) throw REQUIRED_REFERENCE.create(result);
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        list.forEach(builder::suggest);
        return builder.buildFuture();
    }

}
