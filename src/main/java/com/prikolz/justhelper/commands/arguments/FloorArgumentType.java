package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class FloorArgumentType implements ArgumentType<Integer> {

    public static final DynamicCommandExceptionType MUST_BE_IN_DEV = new DynamicCommandExceptionType((object) -> Component.literal("Недоступно вне мира разработки /dev."));
    public static final DynamicCommandExceptionType FLOOR_NOT_FOUND = new DynamicCommandExceptionType((object) -> TextUtils.minimessage("Этаж {0} не найден!", object));
    public static final StringArgumentType parser = StringArgumentType.greedyString();

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (!DevelopmentWorld.isActive()) throw MUST_BE_IN_DEV.create("");
        String name = parser.parse(reader);
        try {
            int floor = Integer.parseInt(name);
            if (floor < 1) throw FLOOR_NOT_FOUND.create(floor);
            return floor;
        } catch (Throwable ignore) {}
        var describes = DevelopmentWorld.describes.plainDescribes;
        for (int floor : describes.keySet()) {
            var describe = describes.get(floor);
            if (describe.equals(name)) return floor;
        }
        throw FLOOR_NOT_FOUND.create(name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (DevelopmentWorld.isActive()) {
            var describes = DevelopmentWorld.describes.plainDescribes;
            describes.values().forEach(builder::suggest);
            for (int floor : describes.keySet()) {
                var describe = describes.get(floor);
                builder.suggest(describe);
            }
        }
        return builder.buildFuture();
    }
}
