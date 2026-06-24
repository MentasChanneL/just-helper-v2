package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class Variable extends DevValue {

    public static String type = "variable";
    public static DevValueRegistry<Variable> registry = DevValueRegistry.create(
            Variable.type,
            nbt -> {
                var scope = nbt.getString("scope").orElse(null);
                var variable = nbt.getString("variable").orElse(null);
                if (scope == null || variable == null) throw new NullPointerException("scope=" + scope + " variable=" + variable);
                return new Variable(Scope.getByID(scope), variable);
            },
            (value, nbt) -> {
                nbt.put("scope", StringTag.valueOf(value.scope.id));
                nbt.put("variable", StringTag.valueOf(value.variable));
            }
    );

    public Scope scope;
    public String variable;

    public Variable(Scope scope, String variable) {
        super(
                Variable.type,
                Items.MAGMA_CREAM,
                "Переменная({name}, {scope})"
        );
        if (scope == null) throw new NullPointerException("Scope is null");
        this.scope = scope;
        this.variable = variable;
    }

    @Override
    public void handleItemStack(ItemStack item) {
        DevelopmentWorld.addToHistory(scope, variable);
        setDecorationText(item, scope.id.toUpperCase().charAt(0) + "", scope.color);
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><yellow>" + this.variable));
        item.set(DataComponents.LORE, TextUtils.lore()
                .line("<gray>Тип: " + scope.display)
                .build()
        );
        setDecorationText(item, scope.id.toUpperCase().charAt(0) + "", scope.color);
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("name", variable),
                Pair.of("scope", scope.id)
        );
    }

    @Override
    public String miniBuilder() {
        return variable + "(" + scope.id + ")";
    }

    public enum Scope {
        GAME("game", "<#ABC4D6>Игровая", 0xABC4D6),
        LOCAL("local", "<green>Локальная", NamedTextColor.GREEN.value()),
        SAVE("save", "<yellow>Сохраненная", NamedTextColor.YELLOW.value()),
        LINE("line", "<aqua>Линейная", NamedTextColor.AQUA.value());

        public static Scope getByID(String id) {
            if (id == null) return null;
            for (Scope type : Scope.values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }

        public final String id;
        public final String display;
        public final int color;

        Scope(String id, String display, int color) {
            this.id = id;
            this.display = display;
            this.color = color;
        }
    }
}
