package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
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

    @Override
    public String miniBuilder() {
        return text;
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

        public Component getNameComponent() {
            return switch (this.id) {
                case "plain" -> TextUtils.minimessage("<sprite:items:item/filled_map> <white>Обычный");
                case "legacy" -> TextUtils.minimessage("<sprite:items:item/field_masoned_banner_pattern> <yellow>Цветной");
                case "json" -> TextUtils.minimessage("<sprite:items:item/green_bundle> <gold>JSON");
                case "minimessage" -> TextUtils.minimessage("<sprite:items:item/knowledge_book> <#9AFF1F>Стилизуемый");
                default -> TextUtils.minimessage("<sprite:items:item/barrier> <red>Ошибка");
            };
        }
    }
}
