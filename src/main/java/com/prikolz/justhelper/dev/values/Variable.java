package com.prikolz.justhelper.dev.values;

import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.util.Pair;
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
    }

    @Override
    public List<Pair<String, String>> getFormatPlaceholders() {
        return List.of(
                Pair.of("name", variable),
                Pair.of("scope", scope.id)
        );
    }

    public enum Scope {
        GAME("game"),
        LOCAL("local"),
        SAVE("save"),
        LINE("line");

        public static Scope getByID(String id) {
            if (id == null) return null;
            for (Scope type : Scope.values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }

        public final String id;

        Scope(String id) {
            this.id = id;
        }
    }
}
