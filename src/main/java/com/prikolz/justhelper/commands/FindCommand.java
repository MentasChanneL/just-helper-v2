package com.prikolz.justhelper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.prikolz.justhelper.DevelopmentWorld;
import com.prikolz.justhelper.commands.arguments.SignsSearchingArgumentType;
import com.prikolz.justhelper.util.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;

public class FindCommand extends JustHelperCommand {

    public static HashMap<String, String> miniChars = miniChars();

    public static final int PAGE_SIZE = 12;

    public static String toMini(String str) {
        String result = str;
        for(String key : miniChars().keySet()) {
            result = result.replaceAll(key, miniChars.get(key));
        }
        return result;
    }

    public static SignsSearchingArgumentType.InfoPack lastFound = new SignsSearchingArgumentType.InfoPack(List.of());

    public FindCommand() {
        super("find");
    }

    @Override
    public LiteralArgumentBuilder<ClientSuggestionProvider> create(LiteralArgumentBuilder<ClientSuggestionProvider> main) {
        return main.then(
                JustHelperCommands.argument(
                        "text", new SignsSearchingArgumentType()
                ).executes((context -> {
                    var found = SignsSearchingArgumentType.getFound(context, "text");
                    execute(found, 0);
                    return 1;
                })
                )
        );
    }

    public static void execute(SignsSearchingArgumentType.InfoPack found, int page) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        lastFound = found;
        var unpack = found.pack();

        if (unpack.isEmpty()) {
            JustHelperCommand.feedback(
                    "\n<hover:show_text:'Параметры поиска:<yellow>\n{0}'><click:suggest_command:'{1}'><dark_gray> ♯ Ничего не найдено ♯",
                    SignsSearchingArgumentType.lastInput,
                    "/find " + SignsSearchingArgumentType.lastInput,
                    found.pack().size()
            );
            return;
        }

        JustHelperCommand.feedback(
                "\n<hover:show_text:'Параметры поиска:<yellow>\n{0}'><click:suggest_command:'{1}'><yellow> ♯<white> Найдено {2} совпадений:\n",
                SignsSearchingArgumentType.lastInput,
                "/find " + SignsSearchingArgumentType.lastInput,
                found.pack().size()
        );

        var startIndex = page * PAGE_SIZE;

        var hasNextPage = true;
        for (int i = startIndex; i < startIndex + PAGE_SIZE; i++) {
            if (i >= unpack.size()) {
                hasNextPage = false;
                break;
            }
            var info = unpack.get(i);
            JustHelperCommand.feedback( createSignMessage(info) );
        }

        var controllerBuilder = new StringBuilder();
        controllerBuilder.append("<gray><strikethrough:true>    <strikethrough:false> ");
        if (page <= 0) {
            controllerBuilder.append("<dark_gray>←");
        } else {
            controllerBuilder.append("<click:run_command:'/foundlist ").append(page - 1).append("'><hover:show_text:←><yellow>←");
        }
        controllerBuilder.append("<reset> <white>").append(page + 1).append("<gold>/<white>").append(unpack.size() / PAGE_SIZE + 1);
        if (hasNextPage) {
            controllerBuilder.append(" <click:run_command:'/foundlist ").append(page + 1).append("'><hover:show_text:→><yellow>→");
        } else {
            controllerBuilder.append(" <dark_gray>→");
        }
        controllerBuilder.append("<reset> <gray><strikethrough:true>    <strikethrough:false>");
        JustHelperCommand.feedback(Component.empty());
        JustHelperCommand.feedback(controllerBuilder.toString());
    }

    public static Component createSignMessage(SignsSearchingArgumentType.FoundSignInfo info) {
        var sign = info.sign();
        var pos = sign.codePos;
        var lastPrompt = SignsSearchingArgumentType.lastInput;
        String miniLine = toMini("<white>" + pos.line );
        if (pos.line < 10) miniLine = miniLine + " ";
        String clickCommand = "/tp " + (0.5 + pos.blockPos.getX()) + " " + pos.blockPos.getY() + " " + (2.5 + pos.blockPos.getZ());
        String signMainLine = "<gold>● <white>" + info.lines()[0].replaceAll(lastPrompt, "<yellow>" + lastPrompt + "<white>");
        if (info.mainLine() != 0) {
            signMainLine = "<gold>● <gray>" + info.lines()[0] + "<gold>/<white>" + info.lines()[info.mainLine()].replaceAll(lastPrompt, "<yellow>" + lastPrompt + "<white>");
        }
        var hoverTextBuilder = new StringBuilder("<white>");
        for (String line: info.lines()) {
            hoverTextBuilder.append(line.replaceAll(lastPrompt, "<yellow>" + lastPrompt + "<white>")).append('\n');
        }
        hoverTextBuilder.append("<strikethrough:true><gray>                      \n<strikethrough:false>");
        hoverTextBuilder.append("<gray>").append(pos.floor).append(" э | ").append(pos.line).append(" л | ");
        hoverTextBuilder.append(pos.pos).append(" п\n").append("<dark_gray>(Нажмите для\n<dark_gray>телепортации)");
        String hoverText = hoverTextBuilder.toString();
        String floor = "" + pos.floor;
        var describe = DevelopmentWorld.describes.describes.get(pos.floor);
        if (describe != null) floor = "(" + describe + "<yellow>)";
        var result = ComponentUtils.minimessage(
                " <click:run_command:'{3}'><hover:show_text:'{4}'><yellow>{0}{1} {2}",
                floor,
                miniLine,
                signMainLine,
                clickCommand,
                hoverText
        );
        return Component.literal(" ").append(pos.getBlockComponent()).append(result);
    }

    private static HashMap<String, String> miniChars() {
        HashMap<String, String> result = new HashMap<>();

        result.put("0", "₀");
        result.put("1", "₁");
        result.put("2", "₂");
        result.put("3", "₃");
        result.put("4", "₄");
        result.put("5", "₅");
        result.put("6", "₆");
        result.put("7", "₇");
        result.put("8", "₈");
        result.put("9", "₉");
        result.put("-", "₋");
        result.put("\\(", "₍");
        result.put("\\)", "₎");

        //result.put("a", "ᴀ");
        //result.put("b", "ʙ");
        //result.put("c", "ᴄ");
        //result.put("d", "ᴅ");
        //result.put("e", "ᴇ");
        //result.put("f", "ꜰ");
        //result.put("g", "ɢ");
        //result.put("h", "ʜ");
        //result.put("i", "ɪ");
        //result.put("j", "ᴊ");
        //result.put("k", "ᴋ");
        //result.put("l", "ʟ");
        //result.put("m", "ᴍ");
        //result.put("n", "ɴ");
        //result.put("o", "ᴏ");
        //result.put("p", "ᴘ");
        //result.put("q", "ꞯ");
        //result.put("r", "ʀ");
        //result.put("s", "ꜱ");
        //result.put("t", "ᴛ");
        //result.put("u", "ᴜ");
        //result.put("v", "ᴠ");
        //result.put("w", "ᴡ");
        //result.put("x", "x");
        //result.put("y", "ʏ");
        //result.put("z", "ᴢ");

        return result;
    }
}
