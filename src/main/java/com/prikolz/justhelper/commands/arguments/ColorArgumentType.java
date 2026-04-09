package com.prikolz.justhelper.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.network.chat.Component;

public class ColorArgumentType implements ArgumentType<Integer> {

    public static final DynamicCommandExceptionType NOT_COMPLETE_VALUE = new DynamicCommandExceptionType((object) -> Component.literal("HEX цвет должен состоять из 6 символов: " + object));
    public static final DynamicCommandExceptionType ILLEGAL_CHAR = new DynamicCommandExceptionType((object) -> Component.literal("Недопустимый символ: " + object));

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        var hex = "";
        for (int i = 0; i < 6; i++) {
            char c = read(reader, hex);
            if (c == '#' && i == 0) c = read(reader, hex);
            if (Character.digit(c, 16) == -1) throw ILLEGAL_CHAR.create(c);
            hex = hex + c;
        }
        return Integer.parseInt(hex, 16);
    }

    private char read(StringReader reader, String hex) throws CommandSyntaxException {
        if (!reader.canRead()) throw NOT_COMPLETE_VALUE.create(hex);
        return reader.read();
    }
}
