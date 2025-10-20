package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.Items;

import java.util.List;

public class Text extends DevValue {

    public static final String type = "text";
    public static DevValueRegistry<Text> registry = DevValueRegistry.create(
            Text.type,
            nbt -> {
                var parsing = ParsingType.getByID(nbt.getString("parsing").orElse(null));
                var value = nbt.getString("text").orElse(null);
                return new Text(parsing, value);
            },
            (value, nbt) -> {
                nbt.put("parsing", StringTag.valueOf(value.parsingType.id));
                nbt.put("text", StringTag.valueOf(value.text));
            }
    );

    public String text;
    public ParsingType parsingType;

    public Text(ParsingType parsing, String text) {
        super(Text.type, Items.BOOK, "Текст({parse}, {value})");
        if (parsing == null) throw new NullPointerException("Parse is null");
        this.parsingType = parsing;
        this.text = text;
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("parse", parsingType.id),
                Pair.of("value", text)
        );
    }

    public enum ParsingType {
        LEGACY("legacy"),
        PLAIN("plain"),
        JSON("json"),
        MINI_MESSAGE("minimessage");

        public final String id;

        ParsingType(String id) {
            this.id = id;
        }

        public static ParsingType getByID(String id) {
            if (id == null) return null;
            for (var type : ParsingType.values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }
    }
}
