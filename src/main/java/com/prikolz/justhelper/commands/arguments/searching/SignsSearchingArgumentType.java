package com.prikolz.justhelper.commands.arguments.searching;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.codespace.CodeSpace;
import com.prikolz.justhelper.commands.arguments.searching.module.*;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SignsSearchingArgumentType implements ArgumentType<InfoPack> {
    public static final String UNCOMPLETED_SETTINGS =
            "Не полные настройки параметров поиска; настройки заканчиваются на ']'";
    public static final DynamicCommandExceptionType ERROR =
            new DynamicCommandExceptionType((object) -> Component.literal("Ошибка: " + object));

    private static final HashMap<String, SearchModule> searchModules = registerModules();
    private static final StringArgumentType parser = StringArgumentType.greedyString();

    private static RemainingSuggestions suggestionBuilder = new RemainingSuggestions();
    public static String lastInput = "";

    @Override
    public InfoPack parse(StringReader reader) throws CommandSyntaxException {
        suggestionBuilder = new RemainingSuggestions();
        String line = parser.parse(reader);
        var list = new ArrayList<FoundSignInfo>();
        lastInput = line;
        if (line.isBlank()) {
            suggestionBuilder.add("=[");
            return emptyCondition(list);
        } else if (line.equals("=")) {
            suggestionBuilder.cursor++;
            suggestionBuilder.add("[");
        }
        boolean isAdvancedSearch = line.startsWith("=[");
        if (isAdvancedSearch) try {
            suggestionBuilder.cursor += 2;
            return advancedSearch(list, line);
        } catch (Exception e) {
            throw ERROR.create(e.getMessage());
        }
        return standardSearch(list, line);
    }

    private InfoPack advancedSearch(ArrayList<FoundSignInfo> list, String line) {
        var reader = new SearchingSettingsReader(line.substring(2));
        var modules = new ArrayList<SearchModule>();
        StringBuilder label = new StringBuilder();
        suggestionBuilder.cursor += reader.skipSpaces();
        char c = '\0';
        while (reader.canRead()) {
            c = reader.read();
            if (c == '=' || c == ']' || c == '!' || c == '<' || c == '>' || c == ',') {
                boolean inverted = c == '!';
                if (inverted) {
                    suggestionBuilder.cursor += 1;
                    c = reader.read();
                }
                var module = searchModules.get(label.toString());
                if (module == null) {
                    if (label.isEmpty() && c == ']') break;
                    if (c != ']' && c != ',') suggestModules(label.toString());
                    throw new IllegalStateException("Неизвестный параметр поиска '" + label + "'");
                }
                suggestionBuilder.cursor += label.length();
                label = new StringBuilder();
                module = module.create();
                module.inverted = inverted;
                if (module.bodyType == SearchModule.BodyType.REQUIRED && (c == ',' || c == ']')) {
                    suggestionBuilder.addAll(module.operands, o -> o + "");
                    throw new IllegalStateException("Для параметра '" + module.determinant + "' нужны дополнительные настройки");
                }
                modules.add(module);
                if (c == ',') {
                    suggestionBuilder.cursor += reader.skipSpaces() + 1;
                    continue;
                }
                if (c == ']') break;
                if (module.bodyType == SearchModule.BodyType.EMPTY)
                    throw new IllegalStateException("Ожидалось ',' или ']'. Получил: " + c);
                if (!module.operands.contains(c))
                    throw new IllegalStateException("Неизвестный операнд: " + c);
                suggestionBuilder.cursor++;
                module.operand = c;
                var index = reader.index;
                try {
                    module.read(reader, suggestionBuilder);
                } catch (Exception e) {
                    throw new IllegalStateException("Ошибка параметра " + module.determinant + ": " + e.getMessage());
                }
                suggestionBuilder.cursor += reader.index - index;
                if (!reader.canRead()) {
                    suggestionBuilder.add("]");
                    suggestionBuilder.add(",");
                    throw new IllegalArgumentException(UNCOMPLETED_SETTINGS);
                }
                c = reader.read();
                if (c == ',') {
                    suggestionBuilder.cursor += reader.skipSpaces() + 1;
                    continue;
                }
                if (c == ']') break;
                suggestionBuilder.add("]");
                throw new IllegalArgumentException(UNCOMPLETED_SETTINGS);
            } else label.append(c);
        }
        if (c != ']') {
            if (label.isEmpty()) suggestionBuilder.add("]");
            suggestModules(label.toString());
            throw new IllegalArgumentException(UNCOMPLETED_SETTINGS);
        } else {
            suggestionBuilder.cursor += 1;
            if (!label.isEmpty()) {
                if (!suggestModules(label.toString()))
                    throw new IllegalStateException("Неизвестный параметр поиска '" + label + "'");
            }
        }
        suggestionBuilder.cursor += reader.skipSpaces();
        standardSearch(list, reader.tail());
        for (var module : modules) module.applyOn(list);
        return new InfoPack(list);
    }

    private boolean suggestModules(String label) {
        boolean found = false;
        for (var m : searchModules.values())
            if (label == null || label.isEmpty() || m.determinant.startsWith(label)) {
                found = true;
                if (label == null || label.isEmpty()) {
                    suggestionBuilder.add(m.determinant);
                    continue;
                }
                if (m.bodyType == SearchModule.BodyType.EMPTY) {
                    suggestionBuilder.add(m.determinant);
                    suggestionBuilder.add(m.determinant + "!");
                    continue;
                }
                for (char operator : m.operands) {
                    suggestionBuilder.add(m.determinant + operator);
                    suggestionBuilder.add(m.determinant + "!" + operator);
                }
            }
        return found;
    }

    private InfoPack standardSearch(ArrayList<FoundSignInfo> list, String line) {
        if (line.isBlank()) return emptyCondition(list);
        if (line.charAt(0) == '\\') {
            line = line.substring(1);
            if (line.isEmpty()) return emptyCondition(list);
        }
        final String finalLine = line.toLowerCase();
        CodeSpace.signs.values().forEach((v) -> {
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
        for (var sign : CodeSpace.signs.values()) {
            var lines = sign.getLines();
            if (lines.length == 0) continue;
            var info = new FoundSignInfo(lines, 0, sign);
            list.add( info );
            addToSuggestions( info );
        }
        searchModules.get(NearbySort.ID).create().applyOn(list);
        return new InfoPack(list);
    }

    private static void addToSuggestions(FoundSignInfo info) {
        for (var line : info.lines()) {
            if (line.isBlank()) continue;
            suggestionBuilder.add(line);
        }
    }

    public static InfoPack getFound(final CommandContext<?> context, final String name) {
        return context.getArgument(name, InfoPack.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        var list = suggestionBuilder.build(lastInput);
        for (var line : list) builder.suggest(line);
        return builder.buildFuture();
    }

    private static HashMap<String, SearchModule> registerModules() {
        var map = new HashMap<String, SearchModule>();
        new FloorFilter().register(map);
        new NearbySort().register(map);
        new LineFilter().register(map);
        new DistanceFilter("distance").register(map);
        new DistanceFilter("dist").register(map);
        new TextFilter(0).register(map);
        new TextFilter(1).register(map);
        new TextFilter(2).register(map);
        new TextFilter(3).register(map);
        return map;
    }
}
