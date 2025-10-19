package com.prikolz.justhelper.config;

import com.google.gson.JsonPrimitive;
import com.prikolz.justhelper.Config;

public class ChatParameters {
    public Config.Parameter<Boolean, JsonPrimitive> showLineLimit = new Config.Parameter<>(
            true,
            "show_line_limit",
            null,
            (value, logger) -> new JsonPrimitive(value),
            (json, logger) -> json.getAsBoolean()
    );
    public Config.Parameter<Boolean, JsonPrimitive> enableMarkers = new Config.Parameter<>(
            true,
            "enable_functional_markers",
            null,
            (value, logger) -> new JsonPrimitive(value),
            (json, logger) -> json.getAsBoolean()
    );
}
