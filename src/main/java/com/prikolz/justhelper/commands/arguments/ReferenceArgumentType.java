package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReferenceArgumentType<T> implements ArgumentType<ReferenceArgumentType.Holder<T>> {
    public static final DynamicCommandExceptionType UNKNOWN_REFERENCE = new DynamicCommandExceptionType((object) -> Component.literal("Неизвестное значение " + object));
    public static final StringArgumentType reader = StringArgumentType.string();

    public Resolver<T> references;

    public ReferenceArgumentType(List<String> keys, List<T> values) {
        var result = new HashMap<String, T>();
        if (keys.size() != values.size()) throw new IllegalArgumentException("List of keys and list of values has different sizes.");
        for (int i = 0; i < keys.size(); i++) { result.put( keys.get(i), values.get(i) ); }
        references = () -> result;
    }

    public ReferenceArgumentType(Resolver<T> resolver) {
        references = resolver;
    }

    @SafeVarargs
    public static <T extends Enum<?>> ReferenceArgumentType<T> ofEnums(boolean lowercase, T ... enums) {
        var result = new HashMap<String, T>();
        for (T value : enums) {
            if (value == null) {
                result.put("null", null);
            } else  {
                result.put( lowercase ? value.name().toLowerCase() : value.name(), value );
            }
        }
        return new ReferenceArgumentType<>(() -> result);
    }

    @Override
    public Holder<T> parse(StringReader stringReader) throws CommandSyntaxException {
        var map = references.resolve();
        var key = reader.parse(stringReader);
        if (!map.containsKey(key)) throw UNKNOWN_REFERENCE.create(key);
        return new Holder<>(map.get(key));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var map = references.resolve();
        map.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

    public String getKeyOrDefault(T reference, String defaultValue) {
        var map = references.resolve();
        for (String key : map.keySet()) {
            if (map.get(key).equals(reference)) return key;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getReference(CommandContext<?> context, String name) {
        return ((Holder<T>) context.getArgument(name, Holder.class)).value;
    }

    public record Holder<T>(T value) {}
    public interface Resolver<T> { Map<String, T> resolve(); }
}
