package com.prikolz.justhelper.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class DescribeCommand extends JustHelperCommand {
    public DescribeCommand() {
        super("describe");
        this.description = "[Этаж] [Текст] <gray>- Добавляет подпись(название) этажу. Название будет отображается в мире через визуализатор текста, а так-же в командах /find и /floor.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument("floor", IntegerArgumentType.integer(1)).then(
                        JustHelperCommands.argument("describe", StringArgumentType.greedyString()).executes(
                                context -> {
                                    int floor = IntegerArgumentType.getInteger(context, "floor");
                                    var describe = StringArgumentType.getString(context, "describe");
                                    return execute(floor, describe);
                                }
                        )
                ).executes(context -> showFloor(IntegerArgumentType.getInteger(context, "floor")))
        ).executes(context -> showAll());
    }

    public static int showFloor(int floor) {
        if (!DevelopmentWorld.isActive()) return JustHelperCommand.feedback("<yellow>Доступно только в мире кода!");
        String text = DevelopmentWorld.describes.describes.get(floor);
        if (text == null) return JustHelperCommand.feedback("<dark_gray>Описание этажа {0} не найдено", floor);
        JustHelperCommand.feedback(
                "<yellow>{0}{2}<dark_gray> |> <click:suggest_command:'{3}'><hover:show_text:'{4}'><white>{1}",
                floor,
                text,
                floor < 10 ? " " : "",
                "/describe " + floor + " " + text,
                text.replaceAll("<", "\\\\<")
        );
        return 1;
    }

    public static int showAll() {
        if (!DevelopmentWorld.isActive()) return JustHelperCommand.feedback("<yellow>Доступно только в мире кода!");
        JustHelperCommand.feedback("<yellow>v <white>Описания этажей:");
        DevelopmentWorld.describes.describes.keySet().forEach(DescribeCommand::showFloor);
        JustHelperCommand.feedback("<yellow>^");
        return 1;
    }

    public static int execute(int floor, String describe) {
        if (!DevelopmentWorld.isActive()) return JustHelperCommand.feedback("<yellow>Доступно только в мире кода!");
        DevelopmentWorld.describes.describe(floor, describe);
        JustHelperCommand.feedback("<green>Установлено описание <white>{0} <green>этажа:<white> {1}", floor, describe);
        return 1;
    }
}
