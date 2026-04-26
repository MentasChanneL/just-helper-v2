package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GreedyArgumentType<A, T extends ArgumentType<A>> implements ArgumentType<GreedyArgumentType.Holder<A>> {

    public final String split;
    public final T parser;
    public final StringArgumentType greedyParser = StringArgumentType.greedyString();

    public GreedyArgumentType(T parser, String splitRegex) {
        this.split = splitRegex;
        this.parser = parser;
    }

    @Override
    public Holder<A> parse(StringReader stringReader) throws CommandSyntaxException {
        String line = greedyParser.parse(stringReader);
        var args = line.split(split);
        var list = new ArrayList<A>();
        for (var arg : args) list.add( parser.parse(new StringReader(arg)) );

        return new Holder<>(list);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        String[] parts = remaining.split(split, -1);
        String lastArg = parts.length == 0 ? remaining : parts[parts.length - 1];

        // Создаём билдер только для последнего аргумента
        int lastArgStart = builder.getStart() + (remaining.length() - lastArg.length());
        SuggestionsBuilder lastArgBuilder = new SuggestionsBuilder(builder.getInput(), lastArgStart);

        return parser.listSuggestions(context, lastArgBuilder).thenApply(suggestions -> {
            if (suggestions.isEmpty()) return suggestions;

            SuggestionsBuilder resultBuilder = new SuggestionsBuilder(builder.getInput(), builder.getStart());
            String prefix = parts.length > 1 ?
                    remaining.substring(0, remaining.length() - lastArg.length()) : "";

            for (Suggestion suggestion : suggestions.getList()) {
                resultBuilder.suggest(prefix + suggestion.getText());
            }
            return resultBuilder.build();
        });
    }

    @SuppressWarnings("unchecked")
    public static <A> List<A> getArgument(CommandContext<?> context, String name) {
        return ((Holder<A>) context.getArgument(name, Holder.class)).value;
    }

    public record Holder<A>(List<A> value) {}
}
