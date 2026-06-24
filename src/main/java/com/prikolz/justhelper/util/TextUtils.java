package com.prikolz.justhelper.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.world.item.component.ItemLore;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;

public class TextUtils {

    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType((object) -> Component.translatableEscape("argument.component.invalid", object));
    public static final Grammar<Tag> TAG_PARSER = SnbtGrammar.createParser(NbtOps.INSTANCE);
    public static final CommandArgumentParser<Component> PARSER = TAG_PARSER.withCodec(NbtOps.INSTANCE, TAG_PARSER, ComponentSerialization.CODEC, ERROR_INVALID_COMPONENT);

    public static Component minimessage(String minimessage, Object ... placeholders) {
        String message = handlePlaceholders(0, minimessage, placeholders);
        var result = kyoriToMojang( () -> MiniMessage.miniMessage().deserialize(message) );
        return result == null ? Component.literal("[ERROR | CHECK LOGS]") : result;
    }

    public static Component kyoriToMojang(Resolver<net.kyori.adventure.text.Component> resolver) {
        try {
            var kyori = resolver.resolve();
            var json = GsonComponentSerializer.gson().serialize( kyori );
            return PARSER.parseForCommands( new StringReader( json) );
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Deserialize error: {}", t.getMessage());
            JustHelperClient.LOGGER.printStackTrace(t);
            return null;
        }
    }

    public static String toMiniMessage(Component component) {
        try {
            var json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow();
            return MiniMessage.miniMessage().serialize( GsonComponentSerializer.gson().deserializeFromTree(json) );
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Component to minimessage convert error: " + t.getMessage());
        }
        return "[ERROR | CHECK LOGS]";
    }

    private static String handlePlaceholders(int calls, String string, Object ... placeholders) {
        if (calls > 99) return string + "(Infinite recursion)";
        boolean hasPlaceholders = false;
        var builder = new StringBuilder();
        boolean openBracketMode = false;
        var numberReader = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (openBracketMode) {
                if (c == '}') {
                    openBracketMode = false;
                    int i;
                    try { i = Integer.parseInt(numberReader.toString()); } catch (Throwable t) { i = -1; }
                    if (i >= placeholders.length || i < 0) {
                        builder.append('{').append(numberReader).append('}');
                        continue;
                    }
                    Object placeholder = placeholders[i];
                    var placeholderChars = placeholder == null ? "null".toCharArray() : placeholder.toString().toCharArray();
                    for (char placeholderChar : placeholderChars) builder.append(placeholderChar);
                    hasPlaceholders = true;
                    continue;
                }
                numberReader.append(c);
            } else {
                if (c == '{') {
                    openBracketMode = true;
                    numberReader = new StringBuilder();
                    continue;
                }
                builder.append(c);
            }
        }
        if (openBracketMode) builder.append('{').append(numberReader);
        if (hasPlaceholders) return handlePlaceholders(calls + 1, builder.toString(), placeholders);
        return builder.toString();
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

    public static String encodeBase64(String string, Charset charset) {
        return Base64.getEncoder().encodeToString( string.getBytes(charset) );
    }

    public static String decodeBase64(String base64, Charset charset) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new String(decodedBytes, charset);
    }

    public static String copyValue(Object value) {
        return copyValue(value, value);
    }

    public static String copyValue(Object display, Object value) {
        var str = value == null ? "null" : value.toString();
        var key = display == null ? "null" : display.toString();
        return "<hover:show_text:'<tr:chat.copy> " + str + "'><click:copy_to_clipboard:'" + str + "'>" + key;
    }

    public static <T> String joinToString(
            Collection<T> list,
            String separator,
            String prefix,
            String suffix,
            StringResolver<T> resolver
    ) {
        Objects.requireNonNull(separator, "separator cannot be null");
        Objects.requireNonNull(resolver, "resolver cannot be null");
        StringBuilder builder = new StringBuilder();
        if (prefix != null) builder.append(prefix);
        if (list != null) {
            int i = 0;
            for (T object : list) {
                if (i++ > 0) builder.append(separator);
                builder.append(resolver.resolve(object));
            }
        }
        if (suffix != null) builder.append(suffix);
        return builder.toString();
    }

    public static <T> String joinToString(Collection<T> list, String separator, StringResolver<T> resolver) {
        return joinToString(list, separator, null, null, resolver);
    }

    public static LoreBuilder lore() {
        return new LoreBuilder();
    }

    public interface StringResolver<T> {
        String resolve(T object);
    }

    public static class LoreBuilder {
        public ArrayList<Component> lines = new ArrayList<>();

        public LoreBuilder line(String mini, Object ... placeholders) {
            lines.add( minimessage("<!italic><white>" + mini, placeholders) );
            return this;
        }

        public ItemLore build() {
            return new ItemLore(lines);
        }
    }
}
