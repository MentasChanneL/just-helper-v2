package com.prikolz.justhelper.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.dev.values.Text;
import com.prikolz.justhelper.dev.values.Variable;
import net.kyori.adventure.text.format.NamedTextColor;

public class ValueDecorationParameters {

    public Config.Parameter<Boolean, JsonPrimitive> enabled = Parameters.boolParameter("enabled", true, null);

    public Config.Parameter<VariableDecorationsParameter, JsonObject> variable = new Config.Parameter<>(
            new VariableDecorationsParameter(),
            "variable",
            null,
            (value, logger) -> {
                var result = new JsonObject();
                value.characterLimit.write(result, logger);
                value.useNames.write(result, logger);
                value.globalColor.write(result, logger);
                value.saveColor.write(result, logger);
                value.localColor.write(result, logger);
                value.lineColor.write(result, logger);
                return result;
            },
            (json, logger) -> {
                var result = new VariableDecorationsParameter();
                result.characterLimit.read(json, logger);
                result.useNames.read(json, logger);
                result.globalColor.read(json, logger);
                result.saveColor.read(json, logger);
                result.localColor.read(json, logger);
                result.lineColor.read(json, logger);
                return result;
            }
    );

    public Config.Parameter<TextDecorationsParameter, JsonObject> text = new Config.Parameter<>(
            new TextDecorationsParameter(),
            "text",
            null,
            (value, logger) -> {
                var result = new JsonObject();
                value.characterLimit.write(result, logger);
                value.plainColor.write(result, logger);
                value.legacyColor.write(result, logger);
                value.miniColor.write(result, logger);
                value.jsonColor.write(result, logger);
                return result;
            },
            (json, logger) -> {
                var result = new TextDecorationsParameter();
                result.characterLimit.read(json, logger);
                result.plainColor.read(json, logger);
                result.legacyColor.read(json, logger);
                result.miniColor.read(json, logger);
                result.jsonColor.read(json, logger);
                return result;
            }
    );

    public Config.Parameter<NumberDecorationsParameter, JsonObject> number = new Config.Parameter<>(
            new NumberDecorationsParameter(),
            "number",
            null,
            (value, logger) -> {
                var result = new JsonObject();
                value.characterLimit.write(result, logger);
                value.color.write(result, logger);
                return result;
            },
            (json, logger) -> {
                var result = new NumberDecorationsParameter();
                result.characterLimit.read(json, logger);
                result.color.read(json, logger);
                return result;
            }
    );

    public static class VariableDecorationsParameter {
        public int getColor(Variable.Scope scope) {
            switch (scope) {
                case GAME -> {
                    return globalColor.value;
                }
                case SAVE -> {
                    return saveColor.value;
                }
                case LOCAL -> {
                    return localColor.value;
                }
                case LINE -> {
                    return lineColor.value;
                }
            }
            return lineColor.value;
        }

        public Config.Parameter<Integer, JsonPrimitive> characterLimit =
                Parameters.intParameter("character_limit", 1, 0, 10, null);

        public Config.Parameter<Boolean, JsonPrimitive> useNames =
                Parameters.boolParameter("use_variable_name", false, null);

        public Config.Parameter<Integer, JsonPrimitive> globalColor =
                Parameters.colorParameter("global_color", 0xABC4D6, null);

        public Config.Parameter<Integer, JsonPrimitive> saveColor =
                Parameters.colorParameter("save_color", NamedTextColor.YELLOW.value(), null);

        public Config.Parameter<Integer, JsonPrimitive> localColor =
                Parameters.colorParameter("local_color", NamedTextColor.GREEN.value(), null);

        public Config.Parameter<Integer, JsonPrimitive> lineColor =
                Parameters.colorParameter("line_color", NamedTextColor.AQUA.value(), null);
    }

    public static class NumberDecorationsParameter {
        public Config.Parameter<Integer, JsonPrimitive> characterLimit =
                Parameters.intParameter("character_limit", 2, 0, 10, null);

        public Config.Parameter<Integer, JsonPrimitive> color =
                Parameters.colorParameter("color", NamedTextColor.YELLOW.value(), null);
    }

    public static class TextDecorationsParameter {

        public int getColor(Text.ParsingType type) {
            switch (type) {
                case PLAIN -> {
                    return plainColor.value;
                }
                case LEGACY -> {
                    return legacyColor.value;
                }
                case MINI_MESSAGE -> {
                    return miniColor.value;
                }
                case JSON -> {
                    return jsonColor.value;
                }
            }
            return plainColor.value;
        }

        public Config.Parameter<Integer, JsonPrimitive> characterLimit =
                Parameters.intParameter("character_limit", 2, 0, 10, null);

        public Config.Parameter<Integer, JsonPrimitive> plainColor =
                Parameters.colorParameter("plain_color", NamedTextColor.WHITE.value(), null);

        public Config.Parameter<Integer, JsonPrimitive> legacyColor =
                Parameters.colorParameter("legacy_color", NamedTextColor.YELLOW.value(), null);

        public Config.Parameter<Integer, JsonPrimitive> miniColor =
                Parameters.colorParameter("minimessage_color", NamedTextColor.GREEN.value(), null);

        public Config.Parameter<Integer, JsonPrimitive> jsonColor =
                Parameters.colorParameter("json_color", 0xFFB657, null);
    }
}
