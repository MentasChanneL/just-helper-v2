package com.prikolz.justhelper;

import com.google.gson.*;
import com.prikolz.justhelper.config.ChatParameters;
import com.prikolz.justhelper.config.CodeBlockNames;
import com.prikolz.justhelper.config.CommandParameters;
import com.prikolz.justhelper.config.ValueFormats;
import com.prikolz.justhelper.util.FileUtils;
import net.minecraft.util.FileUtil;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.prikolz.justhelper.JustHelperClient.GSON;

public class Config {

    public static Config get() { return JustHelperClient.CONFIG; }

    public final List<Parameter<?, ?>> parameters = new ArrayList<>();

    public Parameter<CodeBlockNames, JsonObject> codeBlockNames = new Parameter<>(
            defaultBlockNames(),
            "block_names",
            parameters,
            (value, logger) -> {
                final var result = new JsonObject();
                value.names().forEach((k, v) -> result.add(k, new JsonPrimitive(v)));
                return result;
            },
            (json, logger) -> {
                var result = new CodeBlockNames(new HashMap<>(), new HashMap<>());
                var keys = json.keySet();
                for (String key : keys) {
                    result.add(key, json.get(key).getAsString());
                }
                return result;
            }
    );
    public Parameter<ChatParameters, JsonObject> chatParameters = new Parameter<>(
            new ChatParameters(),
            "chat_parameters",
            parameters,
            (value, logger) -> {
                var result = new JsonObject();
                value.showLineLimit.write(result, logger);
                value.enableMarkers.write(result, logger);
                return result;
            },
            (json, logger) -> {
                var result = new ChatParameters();
                result.showLineLimit.read(json, logger);
                result.enableMarkers.read(json, logger);
                return result;
            }
    );
    public Parameter<Boolean, JsonPrimitive> showPositionInCode = new Parameter<>(
            true,
            "show_position_in_code",
            parameters,
            (value, logger) -> new JsonPrimitive(value),
            (json, logger) -> json.getAsBoolean()
    );
    public Parameter<Long, JsonPrimitive> commandBufferCD = new Parameter<>(
            700L,
            "command_sending_cooldown",
            parameters,
            (value, logger) -> new JsonPrimitive(value),
            (json, logger) -> json.getAsLong()
    );
    public Parameter<Boolean, JsonPrimitive> teleportAnchor = new Parameter<>(
            true,
            "enable_teleport_anchor",
            parameters,
            (value, logger) -> new JsonPrimitive(value),
            (json, logger) -> json.getAsBoolean()
    );
    public Parameter<Boolean, JsonPrimitive> findEach = new Parameter<>(
            true,
            "enable_each_find_list",
            parameters,
            (value, logger) -> new JsonPrimitive(value),
            (child, logger) -> child.getAsBoolean()
    );
    public Parameter<ValueFormats, JsonObject> valueFormats = new Parameter<>(
            defaultValueFormats(),
            "value_string_formats",
            parameters,
            (value, logger) -> {
                var result = new JsonObject();
                value.formats().forEach((k, v) -> result.add(k, new JsonPrimitive(v)));
                return result;
            },
            (json, logger) -> {
                var map = new HashMap<String, String>();
                for (String key : json.keySet()) map.put(key, json.getAsJsonPrimitive(key).getAsString());
                return new ValueFormats(map);
            }
    );
    public Parameter<CommandParameters, JsonObject> commandParameters = new Parameter<>(
            new CommandParameters(),
            "commands",
            parameters,
            CommandParameters::write,
            (json, logger) -> {
                var result = new CommandParameters();
                result.read(json, logger);
                return result;
            }
    );

    public List<String> read() {
        JustHelperClient.LOGGER.info("Reading config...");
        var logger = new ConfigLogger();
        File configFile = new File(FileUtils.getConfigFolder().getPath() + "/config.json");
        if (!configFile.exists()) {
            logger.log("[W] Config file not found");
            printLogs(reset());
            printLogs(logger);
            return logger.logs;
        }
        try {
            JsonObject json = GSON.fromJson(GSON.newJsonReader(new FileReader(configFile)), JsonObject.class);
            for (var parameter : parameters) parameter.read(json, logger);
            if (logger.configWasUpdated) Files.writeString(configFile.toPath(), GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (Throwable t) {
            logger.log("[E] Fail to read config file: " + t.getMessage());
        }
        printLogs(logger);
        return logger.logs;
    }

    public static void printLogs(ConfigLogger logger) {
        for (String line : logger.logs) {
            if (line.startsWith("[W]")) {
                JustHelperClient.LOGGER.warn(line);
            } else if (line.startsWith("[E]")) {
                JustHelperClient.LOGGER.error(line);
            } else JustHelperClient.LOGGER.info(line);
        }
    }

    public static String getJSON() {
        try {
            return Files.readString( new File(FileUtils.getConfigFolder().getPath() + "/config.json").toPath() );
        } catch (Throwable t) {
            return "Error: " + t.getMessage();
        }
    }

    public ConfigLogger reset() {
        var result = new ConfigLogger();
        try {
            File configFile = new File(FileUtils.getConfigFolder().getPath() + "/config.json");
            FileUtil.createDirectoriesSafe(FileUtils.getConfigFolder().toPath());
            JsonObject json = new JsonObject();
            for (var parameter : parameters) parameter.writeDefault(json, result);
            Files.writeString(configFile.toPath(), GSON.toJson(json));
            result.log("[I] Created new config file");
        } catch (Throwable t) {
            result.log("[E] Config reset error: " + t.getMessage());
        }
        return result;
    }

    public static void saveConfig(String json) {
        try {
            File configFile = new File(FileUtils.getConfigFolder().getPath() + "/config.json");
            FileUtil.createDirectoriesSafe(FileUtils.getConfigFolder().toPath());
            Files.writeString(configFile.toPath(), json);
            JustHelperClient.LOGGER.info("Config saved");
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Write config file error: {}", t.getMessage());
        }
    }

    public static void openConfigFolder() {
        try {
            var way = FileUtils.getConfigFolder().getPath();
            ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe", way);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Fail to open justhelper folder: " + t.getMessage());
        }
    }

    private static CodeBlockNames defaultBlockNames() {
        var result = new CodeBlockNames(new HashMap<>(), new HashMap<>());

        result.add(Blocks.DIAMOND_BLOCK, "<aqua>Событие игрока");
        result.add(Blocks.OAK_PLANKS, "<gold>Если игрок");
        result.add(Blocks.COBBLESTONE, "<green>Действие над игроком");
        result.add(Blocks.LAPIS_ORE, "<blue>Вызвать функцию");
        result.add(Blocks.EMERALD_ORE, "<dark_green>Запустить процесс");
        result.add(Blocks.GOLD_BLOCK, "<yellow>Событие сущности");
        result.add(Blocks.BRICKS, "<green>Если сущность");
        result.add(Blocks.MOSSY_COBBLESTONE, "<dark_green>Действие над сущностью");
        result.add(Blocks.LAPIS_BLOCK, "<aqua>Функция");
        result.add(Blocks.EMERALD_BLOCK, "<green>Процесс");
        result.add(Blocks.IRON_BLOCK, "<yellow>Действие с переменной");
        result.add(Blocks.OBSIDIAN, "<gold>Если переменная");
        result.add(Blocks.NETHERRACK, "<red>Действие над миром");
        result.add(Blocks.RED_NETHER_BRICKS, "<red>Если в мире");
        result.add(Blocks.PRISMARINE, "<green>Повторение");
        result.add(Blocks.REDSTONE_BLOCK, "<red>Событие мира");
        result.add(Blocks.COAL_BLOCK, "<blue>Контроль действий");
        result.add(Blocks.PURPUR_BLOCK, "<light_purple>Выбрать цель");
        result.add(Blocks.END_STONE, "<dark_aqua>Иначе");
        result.add(Blocks.DARK_PRISMARINE, "<dark_purple>Контроллер");

        return result;
    }

    private static ValueFormats defaultValueFormats() {
        var map = new HashMap<String, String>();

        return new ValueFormats(map);
    }

    public static class Parameter<T, A extends JsonElement> {
        public final T defaultValue;
        public T value;
        public final String jsonKey;
        public final JsonResolver<T, A> jsonResolver;
        public final ParameterResolver<T, A> resolver;

        public Parameter(
                T defaultValue,
                String jsonKey,
                List<Parameter<?, ?>> parameters,
                JsonResolver<T, A> jsonResolver,
                ParameterResolver<T, A> resolver
        ) {
            this.defaultValue = defaultValue;
            value = defaultValue;
            this.jsonKey = jsonKey;
            this.jsonResolver = jsonResolver;
            this.resolver = resolver;
            if (parameters != null) parameters.add(this);
        }

        @SuppressWarnings("unchecked")
        public void read(JsonObject json, ConfigLogger logger) {
            if (!json.has(jsonKey)) {
                logger.log("[W] Parameter '" + jsonKey + "' not found. Config was updated.");
                write(json, logger);
                logger.configWasUpdated = true;
                return;
            }
            var child = json.get(jsonKey);
            if (child == null) return;
            try {
                value = resolver.resolve((A) child, logger);
            } catch (Throwable t) {
                value = defaultValue;
                logger.log("[W] Failed to read parameter '" + jsonKey + "'");
            }
        }

        public void write(JsonObject json, ConfigLogger logger) {
            write(json, logger, this.value);
        }

        public void writeDefault(JsonObject json, ConfigLogger logger) {
            write(json, logger, this.defaultValue);
        }

        private void write(JsonObject json, ConfigLogger logger, T value) {
            try {
                A el = jsonResolver.resolve(value, logger);
                json.add(jsonKey, el);
            } catch (Throwable t) {
                logger.log("[W] Failed to write '" + jsonKey + "' parameter");
            }
        }

        @Override
        public String toString() {
            return "Parameter[" + jsonKey + "](value=" + value + " default=" + defaultValue
                    + " key=" + jsonKey + ")";
        }

        public interface ParameterResolver<T, A extends JsonElement> {
            T resolve(A child, ConfigLogger logger);
        }

        public interface JsonResolver<T, A extends JsonElement> {
            A resolve(T value, ConfigLogger logger);
        }
    }
    public static class ConfigLogger {
        public final List<String> logs = new ArrayList<>();
        public boolean configWasUpdated = false;

        public void log(String line) { logs.add(line); }
    }
}
