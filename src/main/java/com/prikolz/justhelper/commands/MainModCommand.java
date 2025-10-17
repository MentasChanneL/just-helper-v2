package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.gui.ConfigScreen;
import com.prikolz.justhelper.gui.LogsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class MainModCommand extends JustHelperCommand {
    public MainModCommand() {
        super("justhelper");
        this.description = "[help/config/logs] <gray>- Главная команда мода JustHelper.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main
                .then(
                    JustHelperCommands.literal("config").executes(context -> executeConfig())
                )
                .then(
                        JustHelperCommands.literal("help").executes(context -> executeHelp())
                )
                .then(
                        JustHelperCommands.literal("logs").executes(context -> executeLogs())
                )
                .executes(context -> execute());
    }

    public int execute() {
        JustHelperCommand.feedback("<yellow>ⓘ<white> Команда <yellow>/{0}<white>:", this.name);
        JustHelperCommand.feedback( helpEntry("help", "Помощь по моду/Все команды.") );
        JustHelperCommand.feedback( helpEntry("config", "Редактирование/Просмотр конфига.") );
        JustHelperCommand.feedback( helpEntry("logs", "Логи(Журнал о работе) мода.") );
        return 1;
    }

    private String helpEntry(String command, String description) {
        return  "  <click:suggest_command:'/" + this.name + " " + command
                + "'><hover:show_text:'/" + this.name + " " + command
                + "'><white>/" + this.name + " <yellow>" + command + " <gray>- " + description;
    }

    public static int executeConfig() {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen( ConfigScreen.create() ));
        return 1;
    }

    public static int executeLogs() {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(new LogsScreen()));
        return 1;
    }

    public int executeHelp() {
        JustHelperCommand.feedback("\n<yellow>JustHelper <white>команды:\n");
        final String pattern = "<yellow>●<white> /{1}<yellow> {2}";
        for (var command : JustHelperCommands.registerOrder) {
            JustHelperCommand.feedback("<hover:show_text:\"{0}\">{0}", pattern, command.name, command.description);
        }
        return 1;
    }
}
