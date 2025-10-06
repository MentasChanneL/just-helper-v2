package com.prikolz.justhelper.util;

import com.google.gson.Gson;
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

public class ComponentUtils {

    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType((object) -> Component.translatableEscape("argument.component.invalid", new Object[]{object}));
    public static final NbtOps OPS = NbtOps.INSTANCE;
    public static final Grammar<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);
    public static final CommandArgumentParser<Component> PARSER = TAG_PARSER.withCodec(OPS, TAG_PARSER, ComponentSerialization.CODEC, ERROR_INVALID_COMPONENT);

    public static Gson gson = new Gson();

    public static Component minimessage(String minimessage) {
        var json = GsonComponentSerializer.gson().serialize( MiniMessage.miniMessage().deserialize(minimessage) );
        try {
            return PARSER.parseForCommands( new StringReader( gson.toJson(json) ) );
        } catch (Throwable t) {
            JustHelperClient.LOGGER.error("Send minimessage error: {}", t.getMessage());
        }
        return Component.literal("[ERROR | CHECK LOGS]");
    }
}
