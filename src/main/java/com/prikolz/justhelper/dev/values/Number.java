package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.DoubleTag;
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
                if (valueTag == null) throw new NullPointerException("Number is null");
                return new Number(valueTag.toString());
            },
            (value, nbt) -> {
                Tag tag = value.bigValue != null
                        ? StringTag.valueOf(value.bigValue.toPlainString())
                        : DoubleTag.valueOf(value.doubleValue);
                nbt.put("number", tag);
            }
    );

    public String stringValue;
    public BigDecimal bigValue;
    public double doubleValue;
    public boolean isValid;

    public Number(String value) {
        super(Number.type, Items.SLIME_BALL, "Число({value})");
        this.stringValue = value;
        try {
            var big = new BigDecimal(value);
            this.isValid = true;
            if (big.compareTo(new BigDecimal(Double.MAX_VALUE)) > 0 ||
                big.compareTo(new BigDecimal(-Double.MAX_VALUE)) < 0
            ) {
                this.doubleValue = 0.0;
                this.bigValue = big;
                return;
            }
            this.doubleValue = big.doubleValue();
            this.bigValue = null;
            return;
        } catch (Throwable t) {
            JustHelperClient.LOGGER.warn("Failed parse {} as BigDecimal", value);
        }
        this.bigValue = null;
        this.doubleValue = 0.0;
        this.isValid = false;
    }

    public String getValue(boolean plain) {
        if (plain) return bigValue == null ? String.valueOf(doubleValue) : bigValue.toPlainString();
        return bigValue == null ? String.valueOf(doubleValue) : bigValue.toString();
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var config = Config.get().valueDecorations.value.number.value;
        var str = getValue(false);
        int limit = config.characterLimit.value;
        if ( str.length() >= 3 && str.charAt(1) == '.' ) limit += 1;
        if (!isValid) {
            str = "⚠" + str;
            limit += 1;
        }
        setDecorationText(item, str, config.color.value, limit);
        TextUtils.lore()
                .line(isValid ? " " : "<yellow>⚠ Ошибка парсинга \"" + stringValue + "\"")
                .line("<gray>Тип: <white>" + (bigValue == null ? "Double" : "BigDecimal"))
                .write(item);
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><red>" + getValue(false)));
        handleItemStack(item);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of( Pair.of("value", getValue(false)) );
    }

    @Override
    public String miniBuilder() {
        return getValue(false);
    }
}
