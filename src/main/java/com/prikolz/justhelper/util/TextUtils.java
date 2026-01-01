package com.prikolz.justhelper.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.prikolz.justhelper.JustHelperClient;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.Grammar;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TextUtils {

    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType((object) -> Component.translatableEscape("argument.component.invalid", new Object[]{object}));
    public static final NbtOps OPS = NbtOps.INSTANCE;
    public static final Grammar<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);
    public static final CommandArgumentParser<Component> PARSER = TAG_PARSER.withCodec(OPS, TAG_PARSER, ComponentSerialization.CODEC, ERROR_INVALID_COMPONENT);

    public static Component minimessage(String minimessage, Object ... placeholders) {
        var i = 0;
        for (Object placeholder : placeholders) {
            minimessage = minimessage.replaceAll("\\{" + i + "}", placeholder.toString());
            i++;
        }
        var json = GsonComponentSerializer.gson().serialize( MiniMessage.miniMessage().deserialize(minimessage) );
        try {
            return PARSER.parseForCommands( new StringReader( json) );
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Send minimessage error: {}", t.getMessage());
        }
        return Component.literal("[ERROR | CHECK LOGS]");
    }

    public static String splitByWord(String string, int charCount) {
        if (string == null) return null;
        var builder = new StringBuilder();
        var i = 0;
        for (char c : string.toCharArray()) {
            if (i >= charCount && (c == ' ' || c == '\n')) {
                builder.append('\n');
                i = 0;
                continue;
            }
            builder.append(c);
            i++;
        }
        return builder.toString();
    }

    public static String encodeBase64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static String copyValue(Object value) {
        var str = value == null ? "null" : value.toString();
        return "<hover:show_text:'<tr:chat.copy> " + str + "'><click:copy_to_clipboard:'" + str + "'>" + str;
    }
}
