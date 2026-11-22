package com.prikolz.justhelper.commands.arguments.searching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class RemainingSuggestions {
    public int cursor = 0;
    private final List<String> elements = new ArrayList<>();

    public void add(String suggest) {
        if (suggest == null || suggest.isEmpty()) return;
        elements.add(suggest);
    }

    public void addAll(Collection<String> suggests) {
        for (var s : suggests) add(s);
    }

    public <T> void addAll(Collection<T> list, Function<T, String> function) {
        for (T o : list) add(function.apply(o));
    }

    public List<String> build(String remaining) {
        List<String> result = new ArrayList<>();
        if (remaining.isEmpty()) {
            return new ArrayList<>(elements);
        }
        var baked = remaining.substring(0, cursor);
        remaining = remaining.substring(cursor);
        for (String suggest : elements) {
            int common = 0;
            int limit = Math.min(remaining.length(), suggest.length());
            // Идём от максимально возможной длины вниз, чтобы найти наибольшее совпадение
            for (int i = limit; i >= 0; i--) {
                if (remaining.endsWith(suggest.substring(0, i))) {
                    common = i;
                    break;
                }
            }
            if (common == 0)
                result.add(baked + suggest);
            else // Склеиваем: remaining + (suggest без общей части)
                result.add(baked + remaining + suggest.substring(common));
        }
        return result;
    }

    @Override
    public String toString() {
        return "SuggestionBuilder{" +
                "elements=" + elements +
                '}';
    }
}
