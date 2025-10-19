package com.prikolz.justhelper.config;

import com.prikolz.justhelper.util.Pair;

import java.util.List;
import java.util.Map;

public record ValueFormats(Map<String, String> formats) {
    public String getFormatted(String type, List<Pair<String, String>> placeholders) {
        var result = formats.get(type);
        if (result == null) return null;
        return format(placeholders, result);
    }

    public static String format(List<Pair<String, String>> placeholders, String value) {
        var result = value;
        for (var placeholder : placeholders) {
            result = result.replaceAll("\\{" + placeholder.first + "}", placeholder.second);
        }
        return result;
    }
}
