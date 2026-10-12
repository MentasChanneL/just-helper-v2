package com.prikolz.justhelper.commands.arguments.searching;

import java.util.ArrayList;
import java.util.List;

public class SearchingSettingsReader {
    public static final String NOT_COMPLETE_ARGUMENT = "Неполный аргумент";

    public final char[] line;
    public int index = 0;

    public SearchingSettingsReader(String line) {
        this.line = line.toCharArray();
    }

    public String tail() {
        return new String(line).substring(index);
    }

    public char peek() {
        if (index >= line.length) throw new IllegalStateException(NOT_COMPLETE_ARGUMENT);
        return line[index];
    }

    public char read() {
        if (index >= line.length) throw new IllegalStateException(NOT_COMPLETE_ARGUMENT);
        return line[index++];
    }

    public void back() {
        index--;
    }

    public boolean canRead() {
        return index < line.length;
    }

    public int skipSpaces() {
        int count = 0;
        while (canRead() && peek() == ' ') {
            read();
            count++;
        }
        return count;
    }

    public String readString(RemainingSuggestions suggestions) {
        if (!canRead()) {
            suggestions.add("\"\"");
            return "";
        }
        boolean captured = false;
        boolean escape = false;
        StringBuilder builder = new StringBuilder();
        char first = read();
        if (first == '"') {
            captured = true;
        }
        else builder.append(first);
        while (canRead()) {
            char c = read();
            if (escape) {
                escape = false;
                builder.append(c);
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (captured) {
                if (c == '"') {
                    return builder.toString();
                }
            } else if (c == ',' || c == ']') {
                back();
                return builder.toString();
            }
            builder.append(c);
        }
        if (captured) {
            suggestions.add("\"");
            throw new IllegalStateException("Строка не закончена");
        }
        return builder.toString();
    }

    public List<String> readList(RemainingSuggestions suggestions) {
        char first = read();
        if (first != '[') {
            suggestions.add("[");
            throw new IllegalStateException("Список должен начинаться с '['. Получен: " + first);
        }
        var result = new ArrayList<String>();
        if (peek() == ']') {
            read();
            return result;
        }
        while (true) {
            var el = readString(suggestions);
            if (!canRead()) {
                if (!result.isEmpty()) suggestions.add(",");
                suggestions.add("]");
                throw new IllegalStateException("Список не закончен, ожидалось ',' или ']'");
            }
            result.add(el);
            char c = read();
            if (c == ']') {
                return result;
            }
            if (c != ',') {
                suggestions.add(",");
                suggestions.add("]");
                throw new IllegalStateException("Список не закончен, ожидалось ',' или ']'. Получен: " + first);
            }
            skipSpaces();
        }
    }
}
