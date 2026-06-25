package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
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

    public static Component deserialize(ParsingType type, String text) {
        var component = switch (type) {
            case PLAIN -> Component.literal(text);
            case JSON -> TextUtils.kyoriToMojang( () -> GsonComponentSerializer.gson().deserialize(text) );
            case LEGACY -> TextUtils.kyoriToMojang( () -> LegacyComponentSerializer.legacy('&').deserialize(text) );
            case MINI_MESSAGE -> TextUtils.minimessage(text);
        };
        return component == null ? Component.literal(text) : component;
    }

    public String text;
    public ParsingType parsingType;

    public Text(ParsingType parsing, String text) {
        super(Text.type, Items.BOOK, "Текст({parse}, {value})");
        if (parsing == null) throw new NullPointerException("Parse is null");
        this.parsingType = parsing;
        this.text = text;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var config = Config.get().valueDecorations.value.text.value;
        setDecorationText(
                item,
                deserialize(parsingType, text).getString(),
                config.getColor(parsingType),
                config.characterLimit.value
        );
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, deserialize(parsingType, text).copy().withStyle(Style.EMPTY.withItalic(false)));
        item.set(DataComponents.LORE, TextUtils.lore()
                .line("<#ABC4D6>Тип: " + parsingType.lang)
                .line("<#ABC4D6>Исходный вид:")
                .line(text)
                .build()
        );
        handleItemStack(item);
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
        LEGACY("legacy", "<yellow>Цветной"),
        PLAIN("plain", "<white>Обычный"),
        JSON("json", "<#FFB657>JSON"),
        MINI_MESSAGE("minimessage", "<green>Стилизуемый");

        public final String id;
        public final String lang;

        ParsingType(String id, String lang) {
            this.id = id;
            this.lang = lang;
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
