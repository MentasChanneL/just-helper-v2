package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.JustHelperClient;
import com.prikolz.justhelper.UpdateChecker;
import com.prikolz.justhelper.commands.arguments.ReferenceArgumentType;
import com.prikolz.justhelper.gui.ConfigScreen;
import com.prikolz.justhelper.gui.LogsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.util.HashMap;

public class MainModCommand extends JustHelperCommand {
    public MainModCommand() {
        super("justhelper");
        this.description = "[help/config/logs] <gray>- Главная команда мода JustHelper.";
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        var configNode = new LineCommand("config")
                .run(context -> executeConfig())
                .literal("reload")
                .run(context -> {
                    var list = JustHelperClient.CONFIG.readFile();
                    feedback("<green>[JustHelper]<white> Конфиг перезагружен. Ошибки: " + list.size());
                    return feedback(list + "");
                }).build();

        var helpNode = new LineCommand("help")
                .run(context -> executeHelp())
                .arg("command", new ReferenceArgumentType<>( () -> {
                    var map = new HashMap<String, JustHelperCommand>();
                    for (var command : Commands.registerOrder) map.put(command.id, command);
                    return map;
                }))
                .run(context -> {
                    var command = ReferenceArgumentType.<JustHelperCommand>getReferences(context, "command").getFirst();
                    printCommand(command, false);
                    return 1;
                }).build();

        return main
                .then(configNode).then(helpNode)
                .then(Commands.literal("logs").executes(context -> executeLogs()))
                .then(Commands.literal("updates").executes(context -> executeUpdates()))
                .executes(context -> execute());
    }

    public int execute() {
        JustHelperCommand.feedback("<yellow>ⓘ<white> Команда <yellow>/{0}<white>:", this.name);
        JustHelperCommand.feedback( helpEntry("help", "Помощь по моду/Все команды.") );
        JustHelperCommand.feedback( helpEntry("config", "Редактирование/Просмотр конфига.") );
        JustHelperCommand.feedback( helpEntry("logs", "Логи(Журнал о работе) мода.") );
        JustHelperCommand.feedback("<gray>Версия мода: <green>{0}", JustHelperClient.versionDate);
        return 1;
    }

    private String helpEntry(String command, String description) {
        return  "  <click:suggest_command:'/" + this.name + " " + command
                + "'><hover:show_text:'/" + this.name + " " + command
                + "'><white>/" + this.name + " <yellow>" + command + " <gray>- " + description;
    }

    public static int executeConfig() {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen( ConfigScreen.create() ));
        return 1;
    }

    public static int executeLogs() {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(new LogsScreen()));
        return 1;
    }

    public void printCommand(JustHelperCommand command, boolean cut) {
        final String pattern = "<yellow>●<white> /{1}<yellow> {2}";
        JustHelperCommand.feedback(
                "<hover:show_text:\"{0}\"><click:run_command:\"/justhelper help {3}\">{0}",
                pattern,
                command.name,
                cut ? (command.description.length() > 100 ? command.description.substring(0, 100) + " <yellow>[...]</yellow>"
                        : command.description) : command.description,
                command.id
        );
    }

    public int executeHelp() {
        JustHelperCommand.feedback("\n<yellow>JustHelper <white>команды:\n");
        for (var command : Commands.registerOrder) printCommand(command, true);
        return 1;
    }

    public int executeUpdates() {
        UpdateChecker.checkUpdates();
        return 1;
    }
}
