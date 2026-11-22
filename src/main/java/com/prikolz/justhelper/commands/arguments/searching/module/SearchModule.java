package com.prikolz.justhelper.commands.arguments.searching.module;

import com.prikolz.justhelper.commands.arguments.searching.FoundSignInfo;
import com.prikolz.justhelper.commands.arguments.searching.RemainingSuggestions;
import com.prikolz.justhelper.commands.arguments.searching.SearchingSettingsReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class SearchModule {
    public final String determinant;
    public final BodyType bodyType;
    public final Set<Character> operands;

    public char operand = '\0';
    public boolean inverted = false;

    public SearchModule(String determinant) {
        this.determinant = determinant;
        this.bodyType = BodyType.EMPTY;
        this.operands = Set.of();
    }

    public SearchModule(String determinant, BodyType bodyType, char operand, char... operands) {
        this.determinant = determinant;
        this.bodyType = bodyType;
        var list = new HashSet<Character>();
        list.add(operand);
        for (char c : operands) list.add(c);
        this.operands = list;
    }

    public abstract SearchModule create();

    public abstract void read(SearchingSettingsReader reader, RemainingSuggestions suggestions);

    public abstract void applyOn(List<FoundSignInfo> list);

    public final void register(HashMap<String, SearchModule> modules) {
        modules.put(determinant, this);
    }

    protected final void filter(List<FoundSignInfo> list, Predicate<FoundSignInfo> predicate) {
        list.removeIf(it -> inverted == predicate.test(it));
    }

    protected final void sort(List<FoundSignInfo> list, Comparator<FoundSignInfo> comparator) {
        if (inverted) list.sort(comparator.reversed());
        else list.sort(comparator);
    }

    protected final LocalPlayer player() { return Minecraft.getInstance().player; }

    public enum BodyType {
        REQUIRED, OPTIONAL, EMPTY
    }

    public static abstract class NumberFilter<T extends Number & Comparable<T>> extends SearchModule {
        public List<T> numbers = List.of();
        public Consumer<RemainingSuggestions> examples;

        public NumberFilter(String determinant, Consumer<RemainingSuggestions> examples) {
            super(determinant, BodyType.REQUIRED, '=', '<', '>');
            this.examples = examples;
        }

        @Override
        public void read(SearchingSettingsReader reader, RemainingSuggestions suggestions) {
            if (!reader.canRead()) {
                if (operand == '=') suggestions.add("[");
                if (examples != null) examples.accept(suggestions);
                throw new IllegalStateException(SearchingSettingsReader.NOT_COMPLETE_ARGUMENT);
            }
            if (reader.peek() == '[' && operand == '=') {
                numbers = reader.readList(suggestions).stream().map(this::toNumber).toList();
                return;
            }
            var label = reader.readString(suggestions);
            numbers = List.of(toNumber(label));
        }

        @Override
        public void applyOn(List<FoundSignInfo> list) {
            switch (operand) {
                case '=' -> filter(list, it -> numbers.contains(getCompareValue(it)));
                case '<' -> {
                    for (T n : numbers) filter(list, it -> getCompareValue(it).compareTo(n) < 0);
                }
                case '>' -> {
                    for (T n : numbers) filter(list, it -> getCompareValue(it).compareTo(n) > 0);
                }
            }
        }

        abstract T toNumber(String value);

        abstract T getCompareValue(FoundSignInfo entry);
    }
}
