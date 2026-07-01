package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.ContextResolver;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigDecimal;
import java.util.List;

public class Number extends DevValue {

    public static final String type = "number";
    public static final DevValueRegistry<Number> registry = DevValueRegistry.create(
            Number.type,
            nbt -> {
                var valueTag = nbt.get("number");
                return switch (valueTag) {
                    case null -> throw new NullPointerException("Number is null");
                    case NumericTag n -> new Number(n.box().toString());
                    case StringTag s -> new Number(s.value());
                    default -> new Number(valueTag.toString());
                };
            },
            (value, nbt) -> {
                try {
                    nbt.put("number", value.dataType.toNbt.resolve(value.value));
                } catch (NullPointerException e) {
                    nbt.put("number", DoubleTag.valueOf(0.0));
                    JustHelperClient.LOGGER.warn("\"{}\" not a number, math function or placeholder", value.stringValue);
                }
            }
    );

    public String stringValue;
    public Object value;
    public DataType dataType;
    public boolean isValid;

    public Number(String value) {
        super(Number.type, Items.SLIME_BALL, "Число({value})");
        this.stringValue = value;
        this.dataType = DataType.UNKNOWN;
        this.value = null;
        this.isValid = false;
        try {
            var big = new BigDecimal(value);
            this.isValid = true;
            if (big.compareTo(new BigDecimal(Double.MAX_VALUE)) > 0 ||
                big.compareTo(new BigDecimal(-Double.MAX_VALUE)) < 0
            ) {
                this.value = big;
                this.dataType = DataType.BIG_DECIMAL;
                return;
            }
            this.value = big.doubleValue();
            this.dataType = DataType.DOUBLE;
        } catch (Throwable t) {
            if (value.startsWith("%math")) {
                this.isValid = true;
                this.value = value;
                this.dataType = DataType.MATH;
                return;
            }
            if (value.startsWith("%")) {
                this.isValid = true;
                this.value = value;
                this.dataType = DataType.PLACEHOLDER;
                return;
            }
            JustHelperClient.LOGGER.warn("Failed define {} as Double, BigDecimal, Math or Placeholder", value);
        }
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var config = Config.get().valueDecorations.value.number.value;
        var str = stringValue;
        int limit = config.characterLimit.value;
        if (isValid) switch (dataType) {
            case DOUBLE, BIG_DECIMAL -> {
                if ( str.charAt(0) == '-' ) limit++;
                if ( str.length() >= 3 && str.charAt(1) == '.' ) limit++;
            }
            case MATH -> {
                str = "∑";
                limit = 1;
            }
            case PLACEHOLDER -> {
                str = "%";
                limit = 1;
            }
        } else {
            str = "⚠";
            limit = 1;
        }
        setDecorationText(item, str, config.color.value, limit);
        TextUtils.lore()
                .line(isValid ? " " : "<yellow>⚠ Ошибка парсинга \"" + stringValue + "\"")
                .line("<gray>Тип: <white>" + dataType.literal)
                .write(item);
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><red>" + stringValue));
        handleItemStack(item);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of( Pair.of("value", stringValue) );
    }

    @Override
    public String miniBuilder() {
        return stringValue;
    }

    public enum DataType {
        DOUBLE("Double", (obj) -> DoubleTag.valueOf( (Double) obj )),
        BIG_DECIMAL("BigDecimal", (obj) -> StringTag.valueOf( obj.toString() )),
        MATH("Функция", (obj) -> StringTag.valueOf( obj.toString() )),
        PLACEHOLDER("Плейсхолдер", (obj) -> StringTag.valueOf( obj.toString() )),
        UNKNOWN("Не определен", (obj) -> StringTag.valueOf( obj.toString() ));

        public final String literal;
        public final ContextResolver<Tag, Object> toNbt;

        DataType(String literal, ContextResolver<Tag, Object> toNbt) {

            this.literal = literal;
            this.toNbt = toNbt;
        }
    }
}
