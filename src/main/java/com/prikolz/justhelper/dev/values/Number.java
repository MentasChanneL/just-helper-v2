package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                nbt.put("number", StringTag.valueOf(value.value));
            }
    );

    public static String validNumber(String text) {
        text = text.replace("\"", "");
        if (text.isEmpty()) return "0";
        if (text.startsWith("%")) return text;
        StringBuilder builder = new StringBuilder();
        boolean dot = false;
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9' || c == 'e' || c == 'E') builder.append(c);
            else if (c == '-' && builder.isEmpty()) builder.append('-');
            else if (c == '.' && !dot) {
                dot = true;
                builder.append('.');
            }
        }
        var result = builder.toString();
        if (result.startsWith(".")) result = "0" + result;
        if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
        if (result.equals("-")) return "0";
        if (result.startsWith("-.")) result = "-0." + result.substring(2);
        if (result.endsWith(".0")) result = result.substring(0, result.length() - 2);
        return result.isEmpty() ? "0" : result;
    }

    public String value;

    public Number(String value) {
        super(Number.type, Items.SLIME_BALL, "Число({value})");
        this.value = value;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        var config = Config.get().valueDecorations.value.number.value;
        var str = validNumber(value);
        int limit = config.characterLimit.value;
        if ( str.length() >= 3 && str.charAt(1) == '.' ) limit += 1;
        setDecorationText(item, str, config.color.value, limit);
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><red>" + validNumber(value)));
        handleItemStack(item);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of( Pair.of("value", this.value) );
    }

    @Override
    public String miniBuilder() {
        return value;
    }
}
