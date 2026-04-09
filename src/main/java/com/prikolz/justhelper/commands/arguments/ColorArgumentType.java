package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class ColorArgumentType implements ArgumentType<Integer> {

    public static final DynamicCommandExceptionType NOT_COMPLETE_HEX_VALUE = new DynamicCommandExceptionType((object) -> Component.literal("HEX цвет должен состоять из 6 символов: " + object));
    public static final DynamicCommandExceptionType NOT_COMPLETE_RGB_VALUE = new DynamicCommandExceptionType((object) -> Component.literal("RGB цвет должен состоять из 3х чисел: " + object));
    public static final DynamicCommandExceptionType ILLEGAL_RGB_VALUE = new DynamicCommandExceptionType((object) -> Component.literal("RGB канал должен быть от 0 до 255: " + object));
    public static final DynamicCommandExceptionType ILLEGAL_CHAR = new DynamicCommandExceptionType((object) -> Component.literal("Недопустимый символ: " + object));

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        var hex = "";
        boolean isHex = false;
        for (int i = 0; i < 6; i++) {
            char c = read(reader, hex);
            if (c == '#' && i == 0) {
                isHex = true;
                c = read(reader, hex);
            }
            if ( !isHex && (c > '9' || hex.length() > 3) ) isHex = true;
            if (c == ' ' && !isHex) return parseRGB(reader, hex + ' ');
            if (Character.digit(c, 16) == -1) throw ILLEGAL_CHAR.create(c);
            hex = hex + c;
        }
        return Integer.parseInt(hex, 16);
    }

    private int parseRGB(StringReader reader, String result) throws CommandSyntaxException {
        var rgb = new int[]{0, 0, 0};
        rgb[0] = Integer.parseInt(result.substring(0, result.length() - 1));
        if (rgb[0] > 255) throw ILLEGAL_RGB_VALUE.create(rgb[0]);
        for(int i = 0; i < 2; i++) {
            if (!reader.canRead()) throw NOT_COMPLETE_RGB_VALUE.create(rgb[i]);
            if (i > 0 && reader.read() != ' ') throw NOT_COMPLETE_RGB_VALUE.create(rgb[i]);
            if (!reader.canRead()) throw NOT_COMPLETE_RGB_VALUE.create(rgb[i]);
            for (int a = 0; a < 3; a++) {
                char c = reader.read();
                if (Character.digit(c, 10) == -1) throw ILLEGAL_CHAR.create(c);
                rgb[i + 1] = rgb[i + 1] * 10 + (c - 48);
                if (!reader.canRead() || reader.peek() == ' ') break;
            }
            if (rgb[i + 1] > 255) throw ILLEGAL_RGB_VALUE.create(rgb[0]);
        }
        return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private char read(StringReader reader, String hex) throws CommandSyntaxException {
        if (!reader.canRead()) throw NOT_COMPLETE_HEX_VALUE.create(hex);
        return reader.read();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        builder.suggest("#FFAA55");
        builder.suggest("255 125 50");
        return builder.buildFuture();
    }
}
