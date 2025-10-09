package com.prikolz.justhelper.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.commands.JustHelperCommands;

import java.util.HashMap;

public class CommandParameters {
    public final HashMap<String, Config.Parameter<Parameter, JsonObject>> parameters = new HashMap<>();

    public void read(JsonObject json, Config.ConfigLogger logger) {
        for (var parameter : parameters.values()) parameter.read(json, logger);
    }

    public JsonObject write(Config.ConfigLogger logger) {
        var result = new JsonObject();
        for (var parameter : parameters.values()) {
            parameter.write(result, logger);
        }
        return result;
    }

    public CommandParameters() {
        for (var command : JustHelperCommands.commands.values()) {
            var parameter = new Config.Parameter<>(
                    new Parameter(command.id),
                    command.id,
                    null,
                    (value, logger) -> {
                        var result = new JsonObject();
                        value.name.write(result, logger);
                        value.enabled.write(result, logger);
                        return result;
                    },
                    (obj, logger) -> {
                        var result = new Parameter(command.id);
                        result.name.read(obj, logger);
                        result.enabled.read(obj, logger);
                        return result;
                    }
            );
            parameters.put(command.id, parameter);
        }
    }

    public Parameter get(String id) {
        var value = parameters.get(id);
        if (value == null) return new Parameter(id);
        return value.value;
    }

    public static class Parameter {
        public final Config.Parameter<String, JsonPrimitive> name;
        public final Config.Parameter<Boolean, JsonPrimitive> enabled;

        public Parameter(String id) {
            this.name = new Config.Parameter<>(
                    id,
                    "name",
                    null,
                    (value, logger) -> new JsonPrimitive(value),
                    (obj, logger) -> obj.getAsString()
            );
            this.enabled = new Config.Parameter<>(
                    true,
                    "enabled",
                    null,
                    (value, logger) -> new JsonPrimitive(value),
                    (obj, logger) -> obj.getAsBoolean()
            );
        }

        public boolean isEnabled() { return enabled.value; }
        public String getName() { return name.value; }
    }
}
