package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.CommandBuffer;
import com.prikolz.justhelper.dev.values.Number;
import com.prikolz.justhelper.dev.values.Text;
import com.prikolz.justhelper.util.JustHelperUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ValueCommand extends JustHelperCommand {

    private final String split;
    private final Type type;

    public ValueCommand(String id, String split, Type type) {
        super(id);
        this.split = split;
        this.type = type;
        this.description = (split == null ? "[Значение]" : "[Значения через '" + split + "']")
                + " <gray>- Короткая версия команды '" + type.desc + "'";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument("arg", StringArgumentType.greedyString()).executes(context -> {
                    var arg = context.getArgument("arg", String.class);
                    var args = split == null ? new String[]{arg} : arg.split(split);
                    for (String a : args) {
                        switch (type) {
                            case TEXT -> JustHelperUtils.addItem(new Text(Text.ParsingType.LEGACY, a).createItemStack());
                            case NUMBER -> JustHelperUtils.addItem(new Number(a).createItemStack());
                        }
                    }
                    return 1;
                })
        );
    }

    public enum Type {
        TEXT("/txt"), NUMBER("/num");

        public final String desc;

        Type(String desc) {
            this.desc = desc;
        }
    }
}
