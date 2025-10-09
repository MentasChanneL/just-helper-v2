package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.dev.SignInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SignsSearchingArgumentType implements ArgumentType<SignsSearchingArgumentType.InfoPack> {

    private static final StringArgumentType parser = StringArgumentType.greedyString();

    private static final List<String> lastFind = new ArrayList<>();
    public static String lastInput = "";

    @Override
    public InfoPack parse(StringReader reader) throws CommandSyntaxException {
        String line = parser.parse(reader);
        var list = new ArrayList<FoundSignInfo>();
        lastFind.clear();
        lastInput = line;
        if (line.isBlank()) return emptyCondition(list);
        boolean isAdvancedSearch = line.charAt(0) == '!';
        if (isAdvancedSearch) return advancedSearch(list, line);
        return standardSearch(list, line);
    }

    private InfoPack advancedSearch(ArrayList<FoundSignInfo> list, String line) {
        line = line.substring(1);
        if (line.isEmpty()) return emptyCondition(list);

        return new InfoPack(list);
    }

    private InfoPack standardSearch(ArrayList<FoundSignInfo> list, String line) {
        if (line.charAt(0) == '\\') {
            line = line.substring(1);
            if (line.isEmpty()) return emptyCondition(list);
        }
        final String finalLine = line.toLowerCase();
        DevelopmentWorld.signs.values().forEach((v) -> {
            var lines = v.getLines();
            var i = 0;
            for (String lineS : lines) {
                if (lineS.toLowerCase().contains(finalLine)) {
                    var info = new FoundSignInfo(lines, i, v);
                    list.add( info );
                    addToSuggestions( info );
                    break;
                }
                i++;
            }
        });
        return new InfoPack(list);
    }

    private InfoPack emptyCondition(ArrayList<FoundSignInfo> list) {
        for (var sign : DevelopmentWorld.signs.values()) {
            var lines = sign.getLines();
            if (lines.length == 0) continue;
            var info = new FoundSignInfo(lines, 0, sign);
            list.add( info );
            addToSuggestions( info );
        }
        return new InfoPack(list);
    }

    private static void addToSuggestions(FoundSignInfo info) {
        for (var line : info.lines) {
            if (line.isBlank()) continue;
            lastFind.add(line);
        }
    }

    public static InfoPack getFound(final CommandContext<?> context, final String name) {
        return context.getArgument(name, InfoPack.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (var sign : lastFind) builder.suggest(sign);
        return builder.buildFuture();
    }

    public record InfoPack(List<FoundSignInfo> pack) {}

    public record FoundSignInfo(String[] lines, int mainLine, SignInfo sign) {}

}
