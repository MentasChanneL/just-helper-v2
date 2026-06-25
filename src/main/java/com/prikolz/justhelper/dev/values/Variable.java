package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.Config;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.util.Pair;
import com.prikolz.justhelper.util.TextUtils;
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

    private void setDecorations(ItemStack item) {
        var config = Config.get().valueDecorations.value.variable.value;
        var name = config.useNames.value ? variable : scope.id.toUpperCase();
        setDecorationText(item, name, config.getColor(scope), config.characterLimit.value);
    }

    @Override
    public void handleItemStack(ItemStack item) {
        DevelopmentWorld.addToHistory(scope, variable);
        setDecorations(item);
    }

    @Override
    public void itemDecoration(ItemStack item) {
        item.set(DataComponents.CUSTOM_NAME, TextUtils.minimessage("<!italic><yellow>" + this.variable));
        item.set(DataComponents.LORE, TextUtils.lore()
                .line("<gray>Тип: " + scope.display)
                .build()
        );
        setDecorations(item);
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
        GAME("game", "<#ABC4D6>Игровая"),
        LOCAL("local", "<green>Локальная"),
        SAVE("save", "<yellow>Сохраненная"),
        LINE("line", "<aqua>Линейная");

        public static Scope getByID(String id) {
            if (id == null) return null;
            for (Scope type : Scope.values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }

        public final String id;
        public final String display;

        Scope(String id, String display) {
            this.id = id;
            this.display = display;
        }
    }
}
