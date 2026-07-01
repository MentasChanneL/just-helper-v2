package com.prikolz.justhelper.config;

import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.util.TextUtils;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public abstract class Parameters {
    public static final Config.Parameter.ParameterResolver<Integer, JsonPrimitive> colorResolver = (json, logger) -> {
        var string = json.getAsString();
        var enumColor = TextUtils.ENamedTextColor.of(string);
        if (enumColor == null) return TextUtils.parseHexColor(string);
        return enumColor.value;
    };

    public static final Config.Parameter.ParameterResolver<Boolean, JsonPrimitive> boolResolver =
            (json, logger) -> json.getAsBoolean();

    public static Config.Parameter.ParameterResolver<Integer, JsonPrimitive> intResolver(int min, int max) {
        return (json, logger) -> Math.min(max, Math.max(min, json.getAsInt()));
    }

    public static Config.Parameter.ParameterResolver<Long, JsonPrimitive> longResolver(long min, long max) {
        return (json, logger) -> Math.min(max, Math.max(min, json.getAsLong()));
    }

    public static Config.Parameter<Integer, JsonPrimitive> intParameter(String name, int defaultValue, int min, int max, List<Config.Parameter<?, ?>> parameters) {
        return new Config.Parameter<>(
                defaultValue,
                name,
                parameters,
                (value, logger) -> new JsonPrimitive(value),
                intResolver(min, max)
        );
    }

    public static Config.Parameter<Long, JsonPrimitive> longParameter(String name, long defaultValue, long min, long max, List<Config.Parameter<?, ?>> parameters) {
        return new Config.Parameter<>(
                defaultValue,
                name,
                parameters,
                (value, logger) -> new JsonPrimitive(value),
                longResolver(min, max)
        );
    }

    public static BooleanParameter boolParameter(String name, boolean defaultValue, List<Config.Parameter<?, ?>> parameters) {
        return new BooleanParameter(
                defaultValue,
                name,
                parameters,
                (value, logger) -> new JsonPrimitive(value),
                boolResolver
        );
    }

    public static Config.Parameter<Integer, JsonPrimitive> colorParameter(String name, int defaultValue, List<Config.Parameter<?, ?>> parameters) {
        return new Config.Parameter<>(
                defaultValue,
                name,
                parameters,
                (value, logger) -> {
                    var named = NamedTextColor.namedColor(value);
                    if (named == null) return new JsonPrimitive("#" + Integer.toHexString(value));
                    return new JsonPrimitive(named.toString());
                },
                colorResolver
        );
    }

    public static class BooleanParameter extends Config.Parameter<Boolean, JsonPrimitive> {
        public BooleanParameter(Boolean defaultValue, String jsonKey, List<Config.Parameter<?, ?>> parameters, JsonResolver<Boolean, JsonPrimitive> jsonResolver, ParameterResolver<Boolean, JsonPrimitive> resolver) {
            super(defaultValue, jsonKey, parameters, jsonResolver, resolver);
        }
    }
}
